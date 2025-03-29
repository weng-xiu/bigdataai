package com.bigdataai.dataprocessing.service;

import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.service.DataSourceService;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务实现类
 */
@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private DataSourceService dataSourceService;

    @Override
    public Map<String, Object> executeSqlQuery(Long dataSourceId, String sql) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取数据源
            DataSource dataSource = dataSourceService.getDataSourceById(dataSourceId);
            if (dataSource == null) {
                result.put("success", false);
                result.put("message", "数据源不存在");
                return result;
            }

            // 根据数据源类型加载数据
            Dataset<Row> dataset = loadDatasetFromSource(dataSource);
            if (dataset == null) {
                result.put("success", false);
                result.put("message", "无法加载数据集");
                return result;
            }

            // 创建临时视图
            dataset.createOrReplaceTempView("temp_table");

            // 执行SQL查询
            Dataset<Row> queryResult = sparkSession.sql(sql);

            // 转换结果为Map
            List<String> columns = java.util.Arrays.asList(queryResult.columns());
            List<Map<String, Object>> rows = new java.util.ArrayList<>();

            for (Row row : queryResult.collectAsList()) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    rowMap.put(columns.get(i), row.get(i));
                }
                rows.add(rowMap);
            }

            result.put("success", true);
            result.put("columns", columns);
            result.put("data", rows);
            result.put("count", rows.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "执行SQL查询失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> executeJoinAnalysis(Long primaryDataSourceId, String primaryTable,
                                                 Long joinDataSourceId, String joinTable,
                                                 String joinCondition, List<String> selectFields) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取主数据源
            DataSource primaryDataSource = dataSourceService.getDataSourceById(primaryDataSourceId);
            if (primaryDataSource == null) {
                result.put("success", false);
                result.put("message", "主数据源不存在");
                return result;
            }

            // 获取关联数据源
            DataSource joinDataSource = dataSourceService.getDataSourceById(joinDataSourceId);
            if (joinDataSource == null) {
                result.put("success", false);
                result.put("message", "关联数据源不存在");
                return result;
            }

            // 加载主数据集
            Dataset<Row> primaryDataset = loadDatasetFromSource(primaryDataSource);
            if (primaryDataset == null) {
                result.put("success", false);
                result.put("message", "无法加载主数据集");
                return result;
            }

            // 加载关联数据集
            Dataset<Row> joinDataset = loadDatasetFromSource(joinDataSource);
            if (joinDataset == null) {
                result.put("success", false);
                result.put("message", "无法加载关联数据集");
                return result;
            }

            // 创建临时视图
            primaryDataset.createOrReplaceTempView(primaryTable);
            joinDataset.createOrReplaceTempView(joinTable);

            // 构建SQL查询
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT ");
            if (selectFields != null && !selectFields.isEmpty()) {
                sqlBuilder.append(String.join(", ", selectFields));
            } else {
                sqlBuilder.append("* ");
            }
            sqlBuilder.append(" FROM ").append(primaryTable);
            sqlBuilder.append(" JOIN ").append(joinTable);
            sqlBuilder.append(" ON ").append(joinCondition);

            // 执行SQL查询
            Dataset<Row> joinResult = sparkSession.sql(sqlBuilder.toString());

            // 转换结果为Map
            List<String> columns = java.util.Arrays.asList(joinResult.columns());
            List<Map<String, Object>> rows = new java.util.ArrayList<>();
            
            for (Row row : joinResult.collectAsList()) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    rowMap.put(columns.get(i), row.get(i));
                }
                rows.add(rowMap);
            }

            result.put("success", true);
            result.put("columns", columns);
            result.put("data", rows);
            result.put("count", rows.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "执行关联分析失败: " + e.getMessage());
        }

        return result;
    }
    
    private Dataset<Row> loadDatasetFromSource(DataSource dataSource) {
        try {
            switch (dataSource.getType()) {
                case MYSQL:
                case POSTGRESQL:
                case ORACLE:
                    return sparkSession.read()
                            .format("jdbc")
                            .option("url", dataSource.getConnectionUrl())
                            .option("dbtable", dataSource.getProperties().get("table"))
                            .option("user", dataSource.getUsername())
                            .option("password", dataSource.getPassword())
                            .load();
                case ELASTICSEARCH:
                    return sparkSession.read()
                            .format("org.elasticsearch.spark.sql")
                            .option("es.nodes", dataSource.getConnectionUrl())
                            .option("es.port", dataSource.getProperties().get("port"))
                            .option("es.resource", dataSource.getProperties().get("index"))
                            .load();
                case HDFS:
                    String format = dataSource.getProperties().get("format");
                    if (format == null) {
                        format = "parquet"; // 默认格式
                    }
                    return sparkSession.read()
                            .format(format)
                            .load(dataSource.getConnectionUrl());
                case HBASE:
                    // HBase需要特殊处理，这里简化实现
                    throw new UnsupportedOperationException("HBase数据源加载暂未实现");
                default:
                    throw new IllegalArgumentException("不支持的数据源类型: " + dataSource.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException("加载数据集失败: " + e.getMessage(), e);
        }
    }
}