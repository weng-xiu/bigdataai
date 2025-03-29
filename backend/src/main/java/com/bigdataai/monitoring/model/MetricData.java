package com.bigdataai.monitoring.model;

import java.util.Date;

/**
 * 监控指标数据类
 */
public class MetricData {
    private Date timestamp;    // 时间戳
    private Object value;      // 指标值
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
}