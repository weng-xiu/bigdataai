package com.bigdataai.dataintegration.service;

import com.bigdataai.dataintegration.model.DataSource;

import java.util.Map;

/**
 * 数据收集服务接口
 */
public interface DataCollectorService {
    
    /**
     * 从MySQL数据源收集数据
     * @param dataSource 数据源配置
     * @param tableName 表名
     * @param conditions 查询条件
     * @return 收集的数据条数
     */
    int collectFromMySQL(DataSource dataSource, String tableName, Map<String, Object> conditions);
    
    /**
     * 从MongoDB数据源收集数据
     * @param dataSource 数据源配置
     * @param collectionName 集合名
     * @param query 查询条件
     * @return 收集的数据条数
     */
    int collectFromMongoDB(DataSource dataSource, String collectionName, String query);
    
    /**
     * 从Kafka消息队列收集数据
     * @param dataSource 数据源配置
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @return 收集的数据条数
     */
    int collectFromKafka(DataSource dataSource, String topic, String consumerGroup);
    
    /**
     * 从文件系统收集数据
     * @param dataSource 数据源配置
     * @param filePath 文件路径
     * @param fileFormat 文件格式(csv, json, xml等)
     * @return 收集的数据条数
     */
    int collectFromFileSystem(DataSource dataSource, String filePath, String fileFormat);
    
    /**
     * 从HDFS收集数据
     * @param dataSource 数据源配置
     * @param hdfsPath HDFS路径
     * @param fileFormat 文件格式
     * @return 收集的数据条数
     */
    int collectFromHDFS(DataSource dataSource, String hdfsPath, String fileFormat);
    
    /**
     * 从HBase收集数据
     * @param dataSource 数据源配置
     * @param tableName 表名
     * @param familyName 列族名
     * @param startRow 起始行键
     * @param endRow 结束行键
     * @return 收集的数据条数
     */
    int collectFromHBase(DataSource dataSource, String tableName, String familyName, String startRow, String endRow);
    
    /**
     * 从Elasticsearch收集数据
     * @param dataSource 数据源配置
     * @param indexName 索引名
     * @param query 查询DSL
     * @return 收集的数据条数
     */
    int collectFromElasticsearch(DataSource dataSource, String indexName, String query);
    
    /**
     * 从外部API收集数据
     * @param dataSource 数据源配置
     * @param apiPath API路径
     * @param params 请求参数
     * @return 收集的数据条数
     */
    int collectFromAPI(DataSource dataSource, String apiPath, Map<String, String> params);
}