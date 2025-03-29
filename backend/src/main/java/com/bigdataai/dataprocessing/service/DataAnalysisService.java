package com.bigdataai.dataprocessing.service;

import java.util.List;
import java.util.Map;

/**
 * 数据分析服务接口
 * 负责数据的分析操作，包括SQL查询、关联分析、统计分析等
 */
public interface DataAnalysisService {
    
    /**
     * 执行SQL查询分析
     * @param dataSourceId 数据源ID
     * @param sql SQL查询语句
     * @return 查询结果
     */
    Map<String, Object> executeSqlQuery(Long dataSourceId, String sql);
    
    /**
     * 执行数据关联分析
     * @param primaryDataSourceId 主数据源ID
     * @param primaryTable 主表名
     * @param joinDataSourceId 关联数据源ID
     * @param joinTable 关联表名
     * @param joinCondition 关联条件
     * @param selectFields 选择的字段列表
     * @return 分析结果
     */
    Map<String, Object> executeJoinAnalysis(Long primaryDataSourceId, String primaryTable,
                                           Long joinDataSourceId, String joinTable,
                                           String joinCondition, List<String> selectFields);
    
    /**
     * 执行统计分析
     * @param dataSourceId 数据源ID
     * @param tableName 表名
     * @param groupByFields 分组字段列表
     * @param aggregations 聚合操作配置
     * @return 分析结果
     */
    Map<String, Object> executeStatisticalAnalysis(Long dataSourceId, String tableName,
                                                 List<String> groupByFields, Map<String, String> aggregations);
    
    /**
     * 执行时间序列分析
     * @param dataSourceId 数据源ID
     * @param tableName 表名
     * @param timeField 时间字段
     * @param valueField 值字段
     * @param interval 时间间隔
     * @param aggregateFunction 聚合函数
     * @return 分析结果
     */
    Map<String, Object> executeTimeSeriesAnalysis(Long dataSourceId, String tableName,
                                                String timeField, String valueField,
                                                String interval, String aggregateFunction);
    
    /**
     * 执行相关性分析
     * @param dataSourceId 数据源ID
     * @param tableName 表名
     * @param fields 分析字段列表
     * @return 分析结果
     */
    Map<String, Object> executeCorrelationAnalysis(Long dataSourceId, String tableName, List<String> fields);
    
    /**
     * 执行预测分析
     * @param dataSourceId 数据源ID
     * @param tableName 表名
     * @param features 特征字段列表
     * @param target 目标字段
     * @param algorithm 算法类型
     * @param params 算法参数
     * @return 分析结果
     */
    Map<String, Object> executePredictiveAnalysis(Long dataSourceId, String tableName,
                                                List<String> features, String target,
                                                String algorithm, Map<String, Object> params);
}