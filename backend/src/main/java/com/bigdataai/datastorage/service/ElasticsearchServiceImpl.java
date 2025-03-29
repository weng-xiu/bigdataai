package com.bigdataai.datastorage.service;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch存储服务实现类
 */
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Override
    public boolean createIndex(String indexName, Map<String, Object> settings, Map<String, Object> mappings) {
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            
            if (settings != null && !settings.isEmpty()) {
                request.settings(settings);
            }
            
            if (mappings != null && !mappings.isEmpty()) {
                request.mapping(mappings);
            }
            
            CreateIndexResponse response = elasticsearchClient.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new RuntimeException("创建Elasticsearch索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteIndex(String indexName) {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            AcknowledgedResponse response = elasticsearchClient.indices().delete(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new RuntimeException("删除Elasticsearch索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean indexExists(String indexName) {
        try {
            GetIndexRequest request = new GetIndexRequest(indexName);
            return elasticsearchClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("检查Elasticsearch索引是否存在失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getIndexInfo(String indexName) {
        try {
            GetIndexRequest request = new GetIndexRequest(indexName);
            GetIndexResponse response = elasticsearchClient.indices().get(request, RequestOptions.DEFAULT);
            
            Map<String, Object> indexInfo = new HashMap<>();
            indexInfo.put("settings", response.getSettings());
            indexInfo.put("mappings", response.getMappings());
            indexInfo.put("aliases", response.getAliases());
            
            return indexInfo;
        } catch (IOException e) {
            throw new RuntimeException("获取Elasticsearch索引信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> listIndices() {
        try {
            GetIndexRequest request = new GetIndexRequest("*");
            GetIndexResponse response = elasticsearchClient.indices().get(request, RequestOptions.DEFAULT);
            return new ArrayList<>(Arrays.asList(response.getIndices()));
        } catch (IOException e) {
            throw new RuntimeException("列出Elasticsearch索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean addDocument(String indexName, String id, Map<String, Object> document) {
        try {
            IndexRequest request = new IndexRequest(indexName);
            
            if (id != null && !id.isEmpty()) {
                request.id(id);
            }
            
            request.source(document, XContentType.JSON);
            IndexResponse response = elasticsearchClient.index(request, RequestOptions.DEFAULT);
            
            return response.getResult() == DocWriteResponse.Result.CREATED || 
                   response.getResult() == DocWriteResponse.Result.UPDATED;
        } catch (IOException e) {
            throw new RuntimeException("添加Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean bulkAddDocuments(String indexName, List<Map<String, Object>> documents) {
        try {
            BulkRequest request = new BulkRequest();
            
            for (Map<String, Object> document : documents) {
                String id = (String) document.get("id");
                Map<String, Object> content = new HashMap<>(document);
                content.remove("id"); // 移除id字段，避免存入文档内容
                
                IndexRequest indexRequest = new IndexRequest(indexName);
                if (id != null && !id.isEmpty()) {
                    indexRequest.id(id);
                }
                indexRequest.source(content, XContentType.JSON);
                
                request.add(indexRequest);
            }
            
            BulkResponse response = elasticsearchClient.bulk(request, RequestOptions.DEFAULT);
            return !response.hasFailures();
        } catch (IOException e) {
            throw new RuntimeException("批量添加Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateDocument(String indexName, String id, Map<String, Object> document) {
        try {
            UpdateRequest request = new UpdateRequest(indexName, id);
            request.doc(document, XContentType.JSON);
            
            UpdateResponse response = elasticsearchClient.update(request, RequestOptions.DEFAULT);
            return response.getResult() == DocWriteResponse.Result.UPDATED;
        } catch (IOException e) {
            throw new RuntimeException("更新Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getDocument(String indexName, String id) {
        try {
            GetRequest request = new GetRequest(indexName, id);
            GetResponse response = elasticsearchClient.get(request, RequestOptions.DEFAULT);
            
            if (!response.isExists()) {
                return null;
            }
            
            Map<String, Object> document = response.getSourceAsMap();
            document.put("i