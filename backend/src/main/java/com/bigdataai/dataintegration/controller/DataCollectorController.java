package com.bigdataai.dataintegration.controller;

import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.service.DataCollectorService;
import com.bigdataai.dataintegration.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据采集控制器
 */
@RestController
@RequestMapping("/datacollector")
public class DataCollectorController {

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 从MySQL数据源收集数据
     */
    @PostMapping("/mysql/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromMySQL(
            @PathVariable Long dataSourceId,
            @RequestParam String tableName,
            @RequestBody(required = false) Map<String, Object> conditions) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromMySQL(dataSource, tableName, conditions);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从MySQL收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从MongoDB数据源收集数据
     */
    @PostMapping("/mongodb/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromMongoDB(
            @PathVariable Long dataSourceId,
            @RequestParam String collectionName,
            @RequestBody(required = false) String query) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromMongoDB(dataSource, collectionName, query);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从MongoDB收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从Kafka消息队列收集数据
     */
    @PostMapping("/kafka/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromKafka(
            @PathVariable Long dataSourceId,
            @RequestParam String topic,
            @RequestParam String consumerGroup) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromKafka(dataSource, topic, consumerGroup);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从Kafka收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从文件系统收集数据
     */
    @PostMapping("/filesystem/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromFileSystem(
            @PathVariable Long dataSourceId,
            @RequestParam String filePath,
            @RequestParam String fileFormat) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromFileSystem(dataSource, filePath, fileFormat);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从文件系统收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从HDFS收集数据
     */
    @PostMapping("/hdfs/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromHDFS(
            @PathVariable Long dataSourceId,
            @RequestParam String hdfsPath,
            @RequestParam String fileFormat) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromHDFS(dataSource, hdfsPath, fileFormat);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从HDFS收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从HBase收集数据
     */
    @PostMapping("/hbase/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromHBase(
            @PathVariable Long dataSourceId,
            @RequestParam String tableName,
            @RequestParam String familyName,
            @RequestParam(required = false) String startRow,
            @RequestParam(required = false) String endRow) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromHBase(dataSource, tableName, familyName, startRow, endRow);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从HBase收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从Elasticsearch收集数据
     */
    @PostMapping("/elasticsearch/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromElasticsearch(
            @PathVariable Long dataSourceId,
            @RequestParam String indexName,
            @RequestBody(required = false) String query) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromElasticsearch(dataSource, indexName, query);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从Elasticsearch收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从外部API收集数据
     */
    @PostMapping("/api/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> collectFromAPI(
            @PathVariable Long dataSourceId,
            @RequestParam String apiPath,
            @RequestBody(required = false) Map<String, String> params) {
        
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            int count = dataCollectorService.collectFromAPI(dataSource, apiPath, params);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "成功从API收集" + count + "条数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "数据收集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}