package com.bigdataai.dataprocessing.controller;

import com.bigdataai.dataprocessing.service.DataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.bigdataai.dataprocessing.dto.ClassifyDataRequest;
import com.bigdataai.dataprocessing.dto.ClusterDataRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据处理控制器
 */
@RestController
@RequestMapping("/dataprocessing")
@Validated // Enable validation for path variables and request parameters if needed
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
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.cleanData(dataSourceId, tableName, rules);
        return ResponseEntity.ok(result);
    }

    /**
     * 数据转换
     */
    @PostMapping("/transform")
    public ResponseEntity<Map<String, Object>> transformData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestBody Map<String, Object> transformations) {
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.transformData(dataSourceId, tableName, transformations);
        return ResponseEntity.ok(result);
    }

    /**
     * 数据聚合
     */
    @PostMapping("/aggregate")
    public ResponseEntity<Map<String, Object>> aggregateData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestBody Map<String, Object> aggregations) {
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.aggregateData(dataSourceId, tableName, aggregations);
        return ResponseEntity.ok(result);
    }

    /**
     * 数据分类分析
     */
    @PostMapping("/classify")
    public ResponseEntity<Map<String, Object>> classifyData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @RequestParam("target") String target,
            @Valid @RequestBody ClassifyDataRequest request) {
        // GlobalExceptionHandler 将处理 @Valid 验证异常、IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.classifyData(dataSourceId, tableName, request.getFeatures(), target, request.getParams());
        return ResponseEntity.ok(result);
    }

    /**
     * 数据聚类分析
     */
    @PostMapping("/cluster")
    public ResponseEntity<Map<String, Object>> clusterData(
            @RequestParam("dataSourceId") Long dataSourceId,
            @RequestParam("tableName") String tableName,
            @Valid @RequestBody ClusterDataRequest request) {
        // GlobalExceptionHandler 将处理 @Valid 验证异常、IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.clusterData(dataSourceId, tableName, request.getFeatures(), request.getParams());
        return ResponseEntity.ok(result);
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
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.mineAssociationRules(
                dataSourceId, tableName, itemColumn, transactionColumn, params);
        return ResponseEntity.ok(result);
    }

    /**
     * 提交批处理任务
     */
    @PostMapping("/batch-job")
    public ResponseEntity<Map<String, Object>> submitBatchJob(
            @RequestParam("jobName") String jobName,
            @RequestParam("jobType") String jobType,
            @RequestBody Map<String, Object> params) {
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.submitBatchJob(jobName, jobType, params);
        return ResponseEntity.ok(result);
    }

    /**
     * 提交流处理任务
     */
    @PostMapping("/streaming-job")
    public ResponseEntity<Map<String, Object>> submitStreamingJob(
            @RequestParam("jobName") String jobName,
            @RequestParam("sourceId") Long sourceId,
            @RequestBody Map<String, Object> params) {
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.submitStreamingJob(jobName, sourceId, params);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/job/{jobId}/status")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable("jobId") String jobId) {
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.getJobStatus(jobId);
        return ResponseEntity.ok(result);
    }

    /**
     * 停止任务
     */
    @PostMapping("/job/{jobId}/stop")
    public ResponseEntity<Map<String, Object>> stopJob(@PathVariable("jobId") String jobId) {
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        boolean success = dataProcessingService.stopJob(jobId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "任务已停止" : "停止任务失败");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务结果
     */
    @GetMapping("/job/{jobId}/result")
    public ResponseEntity<Map<String, Object>> getJobResult(@PathVariable("jobId") String jobId) {
        // GlobalExceptionHandler 将处理 IllegalArgumentException 和其他 Exception
        Map<String, Object> result = dataProcessingService.getJobResult(jobId);
        return ResponseEntity.ok(result);
    }
}