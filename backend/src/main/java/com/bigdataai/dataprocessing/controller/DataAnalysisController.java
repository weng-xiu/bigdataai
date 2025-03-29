package com.bigdataai.dataprocessing.controller;

import com.bigdataai.dataprocessing.service.DataAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据分析控制器
 * 提供数据分析相关的API接口
 */
@RestController
@RequestMapping("/api/data-analysis")
public class DataAnalysisController {

    @Autowired
    private DataAnalysisService dataAnalysisService;

    /**
     * 执行SQL查询分析
     * @param dataSourceId 数据源ID
     * @param params 包含SQL查询语句的参数
     * @return 查询结果
     */
    @PostMapping("/sql/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> executeSqlQuery(
            @PathVariable Long dataSourceId,
            @RequestBody Map<String, Object> params) {
        String sql = (String) params.get("sql");
        Map<String, Object> result = dataAnalysisService.executeSqlQuery(dataSourceId, sql);
        return ResponseEntity.ok(result);
    }

    /**
     * 执行数据关联分析
     * @param params 关联分析参数
     * @return 分析结果
     */
    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> executeJoinAnalysis(
            @RequestBody Map<String, Object> params) {
        Long primaryDataSourceId = Long.valueOf(params.get("primaryDataSourceId").toString());
        String primaryTable = (String) params.get("primaryTable");
        Long joinDataSourceId = Long.valueOf(params.get("joinDataSourceId").toString());
        String joinTable = (String) params.get("joinTable");
        String joinCondition = (String) params.get("joinCondition");
        @SuppressWarnings("unchecked")
        List<String> selectFields = (List<String>) params.get("selectFields");

        Map<String, Object> result = dataAnalysisService.executeJoinAnalysis(
                primaryDataSourceId, primaryTable, joinDataSourceId, joinTable, joinCondition, selectFields);
        return ResponseEntity.ok(result);
    }

    /**
     * 执行统计分析
     * @param dataSourceId 数据源ID
     * @param params 统计分析参数
     * @return 分析结果
     */
    @PostMapping("/statistics/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> executeStatisticalAnalysis(
            @PathVariable Long dataSourceId,
            @RequestBody Map<String, Object> params) {
        String tableName = (String) params.get("tableName");
        @SuppressWarnings("unchecked")
        List<String> groupByFields = (List<String>) params.get("groupByFields");
        @SuppressWarnings("unchecked")
        Map<String, String> aggregations = (Map<String, String>) params.get("aggregations");

        Map<String, Object> result = dataAnalysisService.executeStatisticalAnalysis(
                dataSourceId, tableName, groupByFields, aggregations);
        return ResponseEntity.ok(result);
    }

    /**
     * 执行时间序列分析
     * @param dataSourceId 数据源ID
     * @param params 时间序列分析参数
     * @return 分析结果
     */
    @PostMapping("/time-series/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> executeTimeSeriesAnalysis(
            @PathVariable Long dataSourceId,
            @RequestBody Map<String, Object> params) {
        String tableName = (String) params.get("tableName");
        String timeField = (String) params.get("timeField");
        String valueField = (String) params.get("valueField");
        String interval = (String) params.get("interval");
        String aggregateFunction = (String) params.get("aggregateFunction");

        Map<String, Object> result = dataAnalysisService.executeTimeSeriesAnalysis(
                dataSourceId, tableName, timeField, valueField, interval, aggregateFunction);
        return ResponseEntity.ok(result);
    }

    /**
     * 执行相关性分析
     * @param dataSourceId 数据源ID
     * @param params 相关性分析参数
     * @return 分析结果
     */
    @PostMapping("/correlation/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> executeCorrelationAnalysis(
            @PathVariable Long dataSourceId,
            @RequestBody Map<String, Object> params) {
        String tableName = (String) params.get("tableName");
        @SuppressWarnings("unchecked")
        List<String> fields = (List<String>) params.get("fields");

        Map<String, Object> result = dataAnalysisService.executeCorrelationAnalysis(
                dataSourceId, tableName, fields);
        return ResponseEntity.ok(result);
    }

    /**
     * 执行预测分析
     * @param dataSourceId 数据源ID
     * @param params 预测分析参数
     * @return 分析结果
     */
    @PostMapping("/prediction/{dataSourceId}")
    public ResponseEntity<Map<String, Object>> executePredictiveAnalysis(
            @PathVariable Long dataSourceId,
            @RequestBody Map<String, Object> params) {
        String tableName = (String) params.get("tableName");
        @SuppressWarnings("unchecked")
        List<String> features = (List<String>) params.get("features");
        String target = (String) params.get("target");
        String algorithm = (String) params.get("algorithm");
        @SuppressWarnings("unchecked")
        Map<String, Object> algorithmParams = (Map<String, Object>) params.get("params");

        Map<String, Object> result = dataAnalysisService.executePredictiveAnalysis(
                dataSourceId, tableName, features, target, algorithm, algorithmParams);
        return ResponseEntity.ok(result);
    }
}