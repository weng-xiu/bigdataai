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
     * 添加告警规则
     * @param metricName 指标名称
     * @param threshold 阈值
     * @param operator 比较运算符
     * @param alertLevel 告警级别
     */
    void addAlertRule(String metricName, double threshold, String operator, String alertLevel);

    /**
     * 获取历史指标数据
     * @param metricName 指标名称
     * @return 该指标的历史数据列表
     */
    List<MetricData> getMetricHistory(String metricName);

    /**
     * 获取数据存储状态
     * @return 包含数据存储状态信息的Map
     */
    Map<String, Object> getDataStorageStatus();
}