package com.bigdataai.datastorage.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据存储服务实现类
 */
@Service
public class DataStorageServiceImpl implements DataStorageService {

    @Value("${hadoop.fs.defaultFS}")
    private String hdfsUrl;

    @Value("${hadoop.user}")
    private String hadoopUser;

    @Value("${hbase.zookeeper.quorum}")
    private String hbaseZookeeperQuorum;

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port}")
    private int elasticsearchPort;

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    /**
     * 获取HDFS文件系统实例
     */
    private FileSystem getFileSystem() throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsUrl);
        System.setProperty("HADOOP_USER_NAME", hadoopUser);
        return FileSystem.get(conf);
    }

    /**
     * 获取HBase连接
     */
    private Connection getHBaseConnection() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", hbaseZookeeperQuorum);
        return ConnectionFactory.createConnection(conf);
    }

    @Override
    public boolean storeToHDFS(String dataPath, byte[] data, boolean overwrite) {
        try {
            FileSystem fs = getFileSystem();
            Path path = new Path(dataPath);

            // 检查父目录是否存在，不存在则创建
            Path parent = path.getParent();
            if (parent != null && !fs.exists(parent)) {
                fs.mkdirs(parent);
            }

            // 写入数据
            try (FSDataOutputStream outputStream = fs.create(path, overwrite)) {
                outputStream.write(data);
                outputStream.flush();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean storeToHDFS(String dataPath, InputStream inputStream, boolean overwrite) {
        try {
            FileSystem fs = getFileSystem();
            Path path = new Path(dataPath);

            // 检查父目录是否存在，不存在则创建
            Path parent = path.getParent();
            if (parent != null && !fs.exists(parent)) {
                fs.mkdirs(parent);
            }

            // 写入数据
            try (FSDataOutputStream outputStream = fs.create(path, overwrite)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] readFromHDFS(String dataPath) {
        try {
            FileSystem fs = getFileSystem();
            Path path = new Path(dataPath);

            if (!fs.exists(path)) {
                throw new IOException("File not found: " + dataPath);
            }

            try (FSDataInputStream inputStream = fs.open(path)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> listHDFSDirectory(String dirPath) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            FileSystem fs = getFileSystem();
            Path path = new Path(dirPath);

            if (!fs.exists(path)) {
                throw new IOException("Directory not found: " + dirPath);
            }

            FileStatus[] fileStatuses = fs.listStatus(path);
            for (FileStatus status : fileStatuses) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("path", status.getPath().toString());
                fileInfo.put("name", status.getPath().getName());
                fileInfo.put("isDirectory", status.isDirectory());
                fileInfo.put("length", status.getLen());
                fileInfo.put("modificationTime", status.getModificationTime());
                fileInfo.put("owner", status.getOwner());
                fileInfo.put("group", status.getGroup());
                fileInfo.put("permission", status.getPermission().toString());
                result.add(fileInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean deleteHDFSPath(String path, boolean recursive) {
        try {
            FileSystem fs = getFileSystem();
            Path hdfsPath = new Path(path);

            if (!fs.exists(hdfsPath)) {
                return false;
            }

            return fs.delete(hdfsPath, recursive);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean storeToHBase(String tableName, String rowKey, String familyName, List<String> qualifiers, List<String> values) {
        if (qualifiers.size() != values.size()) {
            throw new IllegalArgumentException("Qualifiers and values must have the same size");
        }

        try (Connection connection = getHBaseConnection()) {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));

            for (int i = 0; i < qualifiers.size(); i++) {
                put.addColumn(
                        Bytes.toBytes(familyName),
                        Bytes.toBytes(qualifiers.get(i)),
                        Bytes.toBytes(values.get(i))
                );
            }

            table.put(put);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchStoreToHBase(String tableName, List<Map<String, Object>> data) {
        try (Connection connection = getHBaseConnection()) {
            Table table = connection.getTable(TableName.valueOf(tableName));
            List<Put> puts = new ArrayList<>();

            for (Map<String, Object> item : data) {
                String rowKey = (String) item.get("rowKey");
                String familyName = (String) item.get("familyName");
                List<String> qualifiers = (List<String>) item.get("qualifiers");
                List<String> values = (List<String>) item.get("values");

                if (qualifiers.size() != values.size()) {
                    throw new IllegalArgumentException("Qualifiers and values must have the same size");
                }

                Put put = new Put(Bytes.toBytes(rowKey));
                for (int i = 0; i < qualifiers.size(); i++) {
                    put.addColumn(
                            Bytes.toBytes(familyName),
                            Bytes.toBytes(qualifiers.get(i)),
                            Bytes.toBytes(values.get(i))
                    );
                }
                puts.add(put);
            }

            table.put(puts);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, String> readFromHBase(String tableName, String rowKey, String familyName) {
        Map<String, String> result = new HashMap<>();

        try (Connection connection = getHBaseConnection()) {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));

            if (familyName != null && !familyName.isEmpty()) {
                get.addFamily(Bytes.toBytes(familyName));
            }

            Result hbaseResult = table.get(get);
            if (hbaseResult.isEmpty()) {
                return result;
            }

            for (Cell cell : hbaseResult.listCells()) {
                String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                result.put(qualifier, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<Map<String, String>> scanHBase(String tableName, String startRow, String endRow, String familyName) {
        List<Map<String, String>> results = new ArrayList<>();

        try (Connection connection = getHBaseConnection()) {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();

            if (startRow != null && !startRow.isEmpty()) {
                scan.withStartRow(Bytes.toBytes(startRow));
            }

            if (endRow != null && !endRow.isEmpty()) {
                scan.withStopRow(Bytes.toBytes(endRow));
            }

            if (familyName != null && !familyName.isEmpty()) {
                scan.addFamily(Bytes.toBytes(familyName));
            }

            ResultScanner scanner = table.getScanner(scan);
            for (Result result : scanner) {
                Map<String, String> rowData = new HashMap<>();
                rowData.put("rowKey", Bytes.toString(result.getRow()));

                for (Cell cell : result.listCells()) {
                    String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                    rowData.put(qualifier, value);
                }

                results.add(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public boolean deleteFromHBase(String tableName, String rowKey, String familyName) {
        try (Connection connection = getHBaseConnection()) {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));

            if (familyName != null && !familyName.isEmpty()) {
                delete.addFamily(Bytes.toBytes(familyName));
            }

            table.delete(delete);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean indexToElasticsearch(String indexName, String id, Map<String, Object> document) {
        try {
            IndexRequest indexRequest = new IndexRequest(indexName)
                    .id(id)
                    .source(document, XContentType.JSON);

            IndexResponse response = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
            return response.getResult() == DocWriteResponse.Result.CREATED || 
                   response.getResult() == DocWriteResponse.Result.UPDATED;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean bulkIndexToElasticsearch(String indexName, List<Map<String, Object>> documents) {
        try {
            BulkRequest bulkRequest = new BulkRequest();

            for (Map<String, Object> doc : documents) {
                String id = (String) doc.get("id");
                Map<String, Object> document = (Map<String, Object>) doc.get("document");

                IndexRequest indexRequest = new IndexRequest(indexName)
                        .id(id)
                        .source(document, XContentType.JSON);

                bulkRequest.add(indexRequest);
            }

            BulkResponse response = elasticsearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return !response.hasFailures();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> searchFromElasticsearch(String indexName, String query) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> hits = new ArrayList<>();

        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            if (query != null && !query.isEmpty()) {
                // 这里简化处理，实际应用中应该解析query字符串为QueryBuilder对象
                searchSourceBuilder.query(QueryBuilders.wrapperQuery(query));
            } else {
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            }

            searchRequest.source(searchSourceBuilder);
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            result.put("totalHits", response.getHits().getTotalHits().value);
            result.put("maxScore", response.getHits().getMaxScore());

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> hitMap = new HashMap<>();
                hitMap.put("id", hit.getId());
                hitMap.put("score", hit.getScore());
                hitMap.put("source", hit.getSourceAsMap());
                hits.add(hitMap);
            }

            result.put("hits", hits);
        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> getFromElasticsearch(String indexName, String id) {
        try {
            GetRequest getRequest = new GetRequest(indexName, id);
            GetResponse response = elasticsearchClient.get(getRequest, RequestOptions.DEFAULT);

            if (response.isExists()) {
                return response.getSourceAsMap();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteFromElasticsearch(String indexName, String id) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
            DeleteResponse response = elasticsearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
            return response.getResult() == DocWriteResponse.Result.DELETED;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createElasticsearchIndex(String indexName, Map<String, Object> settings, Map<String, Object> mappings) {
        try {
            // 检查索引是否已存在
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            boolean exists = elasticsearchClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (exists) {
                return true;
            }

            // 创建索引
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            if (settings != null && !settings.isEmpty()) {
                createIndexRequest.settings(settings);
            }
            if (mappings != null && !mappings.isEmpty()) {
                createIndexRequest.mapping(mappings);
            }

            elasticsearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteElasticsearchIndex(String indexName) {
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            elasticsearchClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}