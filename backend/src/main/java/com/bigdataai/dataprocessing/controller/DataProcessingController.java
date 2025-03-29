package com.bigdataai.dataprocessing.controller;

import com.bigdataai.dataprocessing.service.DataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据处理控制器
 */
@RestController
@RequestMapping("/dataprocessing")
public class DataProcessingController {

    @Autowired
    private DataProcessingService dataProcessingService;

    /**
     * 数据清洗
     */
    @PostMapping("/clean")
    public ResponseEntity<Map<String, Object>> cleanData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestBody Map<String, Object> rules) {
        
        try {
            Map<String, Object> result = dataProcessingService.cleanData(dataSourceId, tableName, rules);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "数据清洗失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 数据转换
     */
    @PostMapping("/transform")
    public ResponseEntity<Map<String, Object>> transformData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestBody Map<String, Object> transformations) {
        
        try {
            Map<String, Object> result = dataProcessingService.transformData(dataSourceId, tableName, transformations);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "数据转换失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 数据聚合
     */
    @PostMapping("/aggregate")
    public ResponseEntity<Map<String, Object>> aggregateData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestBody Map<String, Object> aggregations) {
        
        try {
            Map<String, Object> result = dataProcessingService.aggregateData(dataSourceId, tableName, aggregations);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "数据聚合失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 数据分类分析
     */
    @PostMapping("/classify")
    public ResponseEntity<Map<String, Object>> classifyData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestParam("target") String target,
            @RequestBody Map<String, Object> request) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) request.get("features");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) request.get("params");
            
            Map<String, Object> result = dataProcessingService.classifyData(dataSourceId, tableName, features, target, params);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "数据分类分析失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 数据聚类分析
     */
    @PostMapping("/cluster")
    public ResponseEntity<Map<String, Object>> clusterData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestBody Map<String, Object> request) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) request.get("features");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) request.get("params");
            
            Map<String, Object> result = dataProcessingService.clusterData(dataSourceId, tableName, features, params);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "数据聚类分析失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 关联规则挖掘
     */
    @PostMapping("/association-rules")
    public ResponseEntity<Map<String, Object>> mineAssociationRules(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestParam("itemColumn") String itemColumn,
            @RequestParam("transactionColumn") String transactionColumn,
            @RequestBody Map<String, Object> params) {
        
        try {
            Map<String, Object> result = dataProcessingService.mineAssociationRules(
                    dataSourceId, tableName, itemColumn, transactionColumn, params);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "关联规则挖掘失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 提交批处理任务
     */
    @PostMapping("/batch-job")
    public ResponseEntity<Map<String, Object>> submitBatchJob(
            @RequestParam("jobName") String jobName,
            @RequestParam("jobType") String jobType,
            @RequestBody Map<String, Object> params) {
        
        try {
            Map<String, Object> result = dataProcessingService.submitBatchJob(jobName, jobType, params);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "提交批处理任务失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 提交流处理任务
     */
    @PostMapping("/streaming-job")
    public ResponseEntity<Map<String, Object>> submitStreamingJob(
            @RequestParam("jobName") String jobName,
            @RequestParam("sourceId") Long sourceId,
            @RequestBody Map<String, Object> params) {
        
        try {
            Map<String, Object> result = dataProcessingService.submitStreamingJob(jobName, sourceId, params);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "提交流处理任务失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/job/{jobId}/status")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable("jobId") String jobId) {
        try {
            Map<String, Object> result = dataProcessingService.getJobStatus(jobId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取任务状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 停止任务
     */
    @PostMapping("/job/{jobId}/stop")
    public ResponseEntity<Map<String, Object>> stopJob(@PathVariable("jobId") String jobId) {
        try {
            boolean success = dataProcessingService.stopJob(jobId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "任务已停止");
            } else {
                result.put("message", "停止任务失败");
            }
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "停止任务失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取任务结果
     */
    @GetMapping("/job/{jobId}/result")
    public ResponseEntity<Map<String, Object>> getJobResult(@PathVariable("jobId") String jobId) {
        try {
            Map<String, Object> result = dataProcessingService.getJobResult(jobId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取任务结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}