package com.bigdataai.monitoring.service;

import java.util.List;
import java.util.Map;

import com.bigdataai.monitoring.model.MetricData;

/**
 * 监控服务接口
 */
public interface MonitoringService {
    /**
     * 获取系统资源使用情况
     * @return 包含系统资源使用信息的Map
     */
    Map<String, Object> getSystemResourceUsage();

    /**
     * 获取数据处理任务状态
     * @return 包含数据处理任务状态信息的列表
     */
    List<Map<String, Object>> getDataProcessingTaskStatus();

    /**
     * 设置告警规则
     * @param metricName 指标名称
     * @param threshold 阈值
     * @param operator 比较运算符
     * @param alertLevel 告警级别
     * @return 操作结果
     */
    Map<String, Object> setAlertRule(String metricName, double threshold, String operator, String alertLevel);

    /**
     * 获取历史指标数据
     * @param metricName 指标名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔
     * @return 包含历史指标数据的Map
     */
    Map<String, Object> getMetricHistory(String metricName, String startTime, String endTime, String interval);

    /**
     * 获取数据存储状态
     * @return 包含数据存储状态信息的Map
     */
    Map<String, Object> getDataStorageStatus();
    
    /**
     * 获取数据接入状态
     * @return 包含数据接入状态信息的列表
     */
    List<Map<String, Object>> getDataIntegrationStatus();
    
    /**
     * 获取告警信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 包含告警信息的列表
     */
    List<Map<String, Object>> getAlerts(String startTime, String endTime);
}