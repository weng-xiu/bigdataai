package com.bigdataai.datastorage.service;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch存储服务接口
 * 负责Elasticsearch的存储操作
 */
public interface ElasticsearchService {
    
    /**
     * 创建索引
     * @param indexName 索引名称
     * @param settings 索引设置
     * @param mappings 索引映射
     * @return 是否创建成功
     */
    boolean createIndex(String indexName, Map<String, Object> settings, Map<String, Object> mappings);
    
    /**
     * 删除索引
     * @param indexName 索引名称
     * @return 是否删除成功
     */
    boolean deleteIndex(String indexName);
    
    /**
     * 检查索引是否存在
     * @param indexName 索引名称
     * @return 是否存在
     */
    boolean indexExists(String indexName);
    
    /**
     * 获取索引信息
     * @param indexName 索引名称
     * @return 索引信息
     */
    Map<String, Object> getIndexInfo(String indexName);
    
    /**
     * 列出所有索引
     * @return 索引列表
     */
    List<String> listIndices();
    
    /**
     * 添加文档
     * @param indexName 索引名称
     * @param id 文档ID，可为null（自动生成）
     * @param document 文档内容
     * @return 是否添加成功
     */
    boolean addDocument(String indexName, String id, Map<String, Object> document);
    
    /**
     * 批量添加文档
     * @param indexName 索引名称
     * @param documents 文档列表
     * @return 是否添加成功
     */
    boolean bulkAddDocuments(String indexName, List<Map<String, Object>> documents);
    
    /**
     * 更新文档
     * @param indexName 索引名称
     * @param id 文档ID
     * @param document 文档内容
     * @return 是否更新成功
     */
    boolean updateDocument(String indexName, String id, Map<String, Object> document);
    
    /**
     * 获取文档
     * @param indexName 索引名称
     * @param id 文档ID
     * @return 文档内容
     */
    Map<String, Object> getDocument(String indexName, String id);
    
    /**
     * 删除文档
     * @param indexName 索引名称
     * @param id 文档ID
     * @return 是否删除成功
     */
    boolean deleteDocument(String indexName, String id);
    
    /**
     * 批量删除文档
     * @param indexName 索引名称
     * @param ids 文档ID列表
     * @return 是否删除成功
     */
    boolean bulkDeleteDocuments(String indexName, List<String> ids);
    
    /**
     * 搜索文档
     * @param indexName 索引名称
     * @param queryBuilder 查询构建器
     * @param from 起始位置
     * @param size 返回数量
     * @return 文档列表
     */
    List<Map<String, Object>> searchDocuments(String indexName, QueryBuilder queryBuilder, int from, int size);
    
    /**
     * 计数文档
     * @param indexName 索引名称
     * @param queryBuilder 查询构建器
     * @return 文档数量
     */
    long countDocuments(String indexName, QueryBuilder queryBuilder);
    
    /**
     * 获取索引统计信息
     * @param indexName 索引名称
     * @return 统计信息
     */
    Map<String, Object> getIndexStats(String indexName);
    
    /**
     * 刷新索引
     * @param indexName 索引名称
     * @return 是否刷新成功
     */
    boolean refreshIndex(String indexName);
}