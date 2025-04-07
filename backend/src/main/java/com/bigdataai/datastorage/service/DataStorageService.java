package com.bigdataai.datastorage.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 数据存储服务接口
 * 负责数据在HDFS、HBase和Elasticsearch等存储系统中的存取操作
 */
public interface DataStorageService {
    
    /**
     * 将数据存储到HDFS
     * @param dataPath HDFS路径
     * @param data 数据内容
     * @param overwrite 是否覆盖已有文件
     * @return 是否存储成功
     */
    boolean storeToHDFS(String dataPath, byte[] data, boolean overwrite);
    
    /**
     * 将输入流数据存储到HDFS
     * @param dataPath HDFS路径
     * @param inputStream 输入流
     * @param overwrite 是否覆盖已有文件
     * @return 是否存储成功
     */
    boolean storeToHDFS(String dataPath, InputStream inputStream, boolean overwrite);
    
    /**
     * 从HDFS读取数据
     * @param dataPath HDFS路径
     * @return 数据内容
     */
    byte[] readFromHDFS(String dataPath);
    
    /**
     * 列出HDFS目录内容
     * @param dirPath 目录路径
     * @return 目录内容列表
     */
    List<Map<String, Object>> listHDFSDirectory(String dirPath);
    
    /**
     * 删除HDFS路径
     * @param path 路径
     * @param recursive 是否递归删除
     * @return 是否删除成功
     */
    boolean deleteHDFSPath(String path, boolean recursive);
    
    /**
     * 存储数据到HBase
     * @param tableName 表名
     * @param rowKey 行键
     * @param familyName 列族名
     * @param qualifiers 列名列表
     * @param values 值列表
     * @return 是否存储成功
     */
    boolean storeToHBase(String tableName, String rowKey, String familyName, List<String> qualifiers, List<String> values);
    
    /**
     * 批量存储数据到HBase
     * @param tableName 表名
     * @param data 数据列表，每个Map包含rowKey、familyName、qualifiers和values
     * @return 是否存储成功
     */
    boolean batchStoreToHBase(String tableName, List<Map<String, Object>> data);
    
    /**
     * 从HBase读取数据
     * @param tableName 表名
     * @param rowKey 行键
     * @param familyName 列族名
     * @return 读取的数据，键为列名，值为列值
     */
    Map<String, String> readFromHBase(String tableName, String rowKey, String familyName);
    
    /**
     * 扫描HBase表
     * @param tableName 表名
     * @param startRow 起始行键
     * @param endRow 结束行键
     * @param familyName 列族名
     * @return 扫描结果列表
     */
    List<Map<String, String>> scanHBase(String tableName, String startRow, String endRow, String familyName);
    
    /**
     * 从HBase删除数据
     * @param tableName 表名
     * @param rowKey 行键
     * @param familyName 列族名
     * @return 是否删除成功
     */
    boolean deleteFromHBase(String tableName, String rowKey, String familyName);
    
    /**
     * 索引文档到Elasticsearch
     * @param indexName 索引名
     * @param id 文档ID
     * @param document 文档内容
     * @return 是否索引成功
     */
    boolean indexToElasticsearch(String indexName, String id, Map<String, Object> document);
    
    /**
     * 批量索引文档到Elasticsearch
     * @param indexName 索引名
     * @param documents 文档列表，每个Map包含id和document
     * @return 是否索引成功
     */
    boolean bulkIndexToElasticsearch(String indexName, List<Map<String, Object>> documents);
    
    /**
     * 从Elasticsearch搜索文档
     * @param indexName 索引名
     * @param query 查询DSL
     * @return 搜索结果
     */
    Map<String, Object> searchFromElasticsearch(String indexName, String query);
    
    /**
     * 从Elasticsearch获取文档
     * @param indexName 索引名
     * @param id 文档ID
     * @return 文档内容
     */
    Map<String, Object> getFromElasticsearch(String indexName, String id);
    
    /**
     * 从Elasticsearch删除文档
     * @param indexName 索引名
     * @param id 文档ID
     * @return 是否删除成功
     */
    boolean deleteFromElasticsearch(String indexName, String id);
    
    /**
     * 创建Elasticsearch索引
     * @param indexName 索引名
     * @param settings 索引设置
     * @param mappings 索引映射
     * @return 是否创建成功
     */
    boolean createElasticsearchIndex(String indexName, Map<String, Object> settings, Map<String, Object> mappings);
    
    /**
     * 删除Elasticsearch索引
     * @param indexName 索引名
     * @return 是否删除成功
     */
    boolean deleteElasticsearchIndex(String indexName);
    
    /**
     * 获取HDFS存储状态信息
     * @return 包含HDFS存储状态的Map
     */
    Map<String, Object> getHdfsStatus();
    
    /**
     * 获取HBase存储状态信息
     * @return 包含HBase存储状态的Map
     */
    Map<String, Object> getHbaseStatus();
    
    /**
     * 获取Elasticsearch存储状态信息
     * @return 包含Elasticsearch存储状态的Map
     */
    Map<String, Object> getElasticsearchStatus();
}