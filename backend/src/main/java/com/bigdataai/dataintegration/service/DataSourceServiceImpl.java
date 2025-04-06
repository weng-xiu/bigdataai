package com.bigdataai.dataintegration.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigdataai.dataintegration.mapper.DataSourceMapper;
import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.model.DataSourceType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DriverManager;
import java.util.*;

/**
 * 数据源服务实现类
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    @Transactional
    public DataSource createDataSource(DataSource dataSource) {
        // 检查数据源名称是否已存在
        if (dataSourceMapper.existsByName(dataSource.getName())) {
            throw new IllegalArgumentException("数据源名称已存在");
        }

        // 设置创建时间和更新时间
        dataSource.setCreateTime(new Date());
        dataSource.setUpdateTime(new Date());

        // 保存数据源
        dataSourceMapper.insert(dataSource);
        return dataSource;
    }

    @Override
    @Transactional
    public DataSource updateDataSource(DataSource dataSource) {
        // 检查数据源是否存在
        DataSource existingDataSource = dataSourceMapper.selectById(dataSource.getId());
        if (existingDataSource == null) {
            throw new IllegalArgumentException("数据源不存在");
        }

        // 检查数据源名称是否已被其他数据源使用
        DataSource dataSourceByName = dataSourceMapper.findByName(dataSource.getName());
        if (dataSourceByName != null && !dataSourceByName.getId().equals(dataSource.getId())) {
            throw new IllegalArgumentException("数据源名称已被其他数据源使用");
        }

        // 更新数据源信息
        existingDataSource.setName(dataSource.getName());
        existingDataSource.setType(dataSource.getType());
        existingDataSource.setConnectionUrl(dataSource.getConnectionUrl());
        existingDataSource.setUsername(dataSource.getUsername());
        existingDataSource.setPassword(dataSource.getPassword());
        existingDataSource.setDescription(dataSource.getDescription());
        existingDataSource.setProperties(dataSource.getProperties());
        existingDataSource.setEnabled(dataSource.getEnabled());
        existingDataSource.setUpdateTime(new Date());

        // 保存更新后的数据源
        dataSourceMapper.updateById(existingDataSource);
        return existingDataSource;
    }

    @Override
    public DataSource getDataSource(Long id) {
        return dataSourceMapper.selectById(id);
    }

    @Override
    public DataSource getDataSourceByName(String name) {
        return dataSourceMapper.findByName(name);
    }

    @Override
    public List<DataSource> getAllDataSources() {
        return dataSourceMapper.selectList(null);
    }

    @Override
    public List<DataSource> getDataSourcesByType(DataSourceType type) {
        return dataSourceMapper.findByType(type);
    }

    @Override
    @Transactional
    public boolean deleteDataSource(Long id) {
        int result = dataSourceMapper.deleteById(id);
        return result > 0;
    }

    @Override
    public Map<String, Object> testConnection(DataSource dataSource) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            switch (dataSource.getType()) {
                case MYSQL:
                    testMySQLConnection(dataSource, result);
                    break;
                case MONGODB:
                    testMongoDBConnection(dataSource, result);
                    break;
                case KAFKA:
                    testKafkaConnection(dataSource, result);
                    break;
                case HDFS:
                    testHDFSConnection(dataSource, result);
                    break;
                case HBASE:
                    testHBaseConnection(dataSource, result);
                    break;
                case ELASTICSEARCH:
                    testElasticsearchConnection(dataSource, result);
                    break;
                default:
                    result.put("message", "不支持的数据源类型: " + dataSource.getType());
            }
        } catch (Exception e) {
            result.put("message", "连接测试失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<String> getTableList(Long dataSourceId) {
        DataSource dataSource = getDataSource(dataSourceId);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在");
        }

        List<String> tables = new ArrayList<>();

        try {
            switch (dataSource.getType()) {
                case MYSQL:
                    tables = getMySQLTables(dataSource);
                    break;
                case MONGODB:
                    tables = getMongoDBCollections(dataSource);
                    break;
                case HBASE:
                    tables = getHBaseTables(dataSource);
                    break;
                case ELASTICSEARCH:
                    tables = getElasticsearchIndices(dataSource);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的数据源类型: " + dataSource.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException("获取表/集合列表失败: " + e.getMessage(), e);
        }

        return tables;
    }

    @Override
    public List<Map<String, Object>> getTableSchema(Long dataSourceId, String tableName) {
        DataSource dataSource = getDataSource(dataSourceId);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在");
        }

        List<Map<String, Object>> schema = new ArrayList<>();

        try {
            switch (dataSource.getType()) {
                case MYSQL:
                    schema = getMySQLTableSchema(dataSource, tableName);
                    break;
                case MONGODB:
                    schema = getMongoDBCollectionSchema(dataSource, tableName);
                    break;
                case HBASE:
                    schema = getHBaseTableSchema(dataSource, tableName);
                    break;
                case ELASTICSEARCH:
                    schema = getElasticsearchIndexSchema(dataSource, tableName);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的数据源类型: " + dataSource.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException("获取表/集合结构失败: " + e.getMessage(), e);
        }

        return schema;
    }

    /**
     * 测试MySQL连接
     */
    private void testMySQLConnection(DataSource dataSource, Map<String, Object> result) throws Exception {
        String url = dataSource.getConnectionUrl();
        String username = dataSource.getUsername();
        String password = dataSource.getPassword();
        
        try (java.sql.Connection connection = DriverManager.getConnection(url, username, password)) {
            // 连接成功，不需要做任何事情
            result.put("success", true);
            result.put("message", "连接成功");
        }
    }
}