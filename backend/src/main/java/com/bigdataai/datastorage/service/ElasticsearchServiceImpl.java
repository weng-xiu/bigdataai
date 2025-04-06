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
import org.elasticsearch.xcontent.XContentType;
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
            document.put("id", response.getId());
            return document;
        } catch (IOException e) {
            throw new RuntimeException("获取Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteDocument(String indexName, String id) {
        try {
            DeleteRequest request = new DeleteRequest(indexName, id);
            DeleteResponse response = elasticsearchClient.delete(request, RequestOptions.DEFAULT);
            
            return response.getResult() == DocWriteResponse.Result.DELETED;
        } catch (IOException e) {
            throw new RuntimeException("删除Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean bulkDeleteDocuments(String indexName, List<String> ids) {
        try {
            BulkRequest request = new BulkRequest();
            
            for (String id : ids) {
                DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
                request.add(deleteRequest);
            }
            
            BulkResponse response = elasticsearchClient.bulk(request, RequestOptions.DEFAULT);
            return !response.hasFailures();
        } catch (IOException e) {
            throw new RuntimeException("批量删除Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> searchDocuments(String indexName, QueryBuilder queryBuilder, int from, int size) {
        try {
            SearchRequest request = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            if (queryBuilder != null) {
                sourceBuilder.query(queryBuilder);
            } else {
                sourceBuilder.query(QueryBuilders.matchAllQuery());
            }
            
            sourceBuilder.from(from);
            sourceBuilder.size(size);
            
            request.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(request, RequestOptions.DEFAULT);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> document = hit.getSourceAsMap();
                document.put("id", hit.getId());
                document.put("score", hit.getScore());
                results.add(document);
            }
            
            return results;
        } catch (IOException e) {
            throw new RuntimeException("搜索Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long countDocuments(String indexName, QueryBuilder queryBuilder) {
        try {
            SearchRequest request = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            if (queryBuilder != null) {
                sourceBuilder.query(queryBuilder);
            } else {
                sourceBuilder.query(QueryBuilders.matchAllQuery());
            }
            
            sourceBuilder.size(0); // 只获取计数，不获取文档内容
            
            request.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(request, RequestOptions.DEFAULT);
            
            return response.getHits().getTotalHits().value;
        } catch (IOException e) {
            throw new RuntimeException("计数Elasticsearch文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getIndexStats(String indexName) {
        try {
            IndicesStatsRequest request = new IndicesStatsRequest();
            request.indices(indexName);
            
            IndicesStatsResponse response = elasticsearchClient.indices().stats(request, RequestOptions.DEFAULT);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("documentCount", response.getTotal().getDocs().getCount());
            stats.put("documentDeleted", response.getTotal().getDocs().getDeleted());
            stats.put("storeSize", response.getTotal().getStore().getSizeInBytes());
            stats.put("indexCount", response.getIndices().size());
            stats.put("totalShards", response.getTotal().getShards().getTotal());
            
            return stats;
        } catch (IOException e) {
            throw new RuntimeException("获取Elasticsearch索引统计信息失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean refreshIndex(String indexName) {
        try {
            RefreshRequest request = new RefreshRequest(indexName);
            elasticsearchClient.indices().refresh(request, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("刷新Elasticsearch索引失败: " + e.getMessage(), e);
        }
    }
}