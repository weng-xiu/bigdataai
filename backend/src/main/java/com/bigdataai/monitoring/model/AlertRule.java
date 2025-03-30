package com.bigdataai.monitoring.model;

import java.io.Serializable;

/**
 * 告警规则类
 */
public class AlertRule implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String metricName;    // 指标名称
    private double threshold;     // 阈值
    private String operator;      // 比较运算符（>, <, >=, <=, ==, !=）
    private String alertLevel;    // 告警级别（INFO, WARNING, ERROR, CRITICAL）
    
    public AlertRule() {
    }
    
    public AlertRule(String metricName, double threshold, String operator, String alertLevel) {
        this.metricName = metricName;
        this.threshold = threshold;
        this.operator = operator;
        this.alertLevel = alertLevel;
    }
    
    public String getMetricName() {
        return metricName;
    }
    
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
    
    public double getThreshold() {
        return threshold;
    }
    
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getAlertLevel() {
        return alertLevel;
    }
    
    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }
    
    @Override
    public String toString() {
        return "AlertRule{" +
                "metricName='" + metricName + '\'' +
                ", threshold=" + threshold +
                ", operator='" + operator + '\'' +
                ", alertLevel='" + alertLevel + '\'' +
                '}';
    }
}