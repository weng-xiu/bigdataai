package com.bigdataai.monitoring.service;

import com.bigdataai.dataprocessing.service.DataProcessingService;
import com.bigdataai.datastorage.service.DataStorageService;
import com.bigdataai.dataintegration.service.DataCollectionTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控服务实现类
 */
@Service
public class MonitoringServiceImpl implements MonitoringService {

    @Autowired
    private DataProcessingService dataProcessingService;

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private DataCollectionTaskService dataCollectionTaskService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 存储告警规则
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();

    // 存储历史指标数据
    private final Map<String, List<MetricData>> metricHistory = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> getSystemResourceUsage() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取操作系统信息
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            result.put("cpuLoad", osBean.getSystemLoadAverage());
            result.put("availableProcessors", osBean.getAvailableProcessors());

            // 获取内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            result.put("heapMemoryUsage", memoryBean.getHeapMemoryUsage().getUsed());
            result.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());
            result.put("nonHeapMemoryUsage", memoryBean.getNonHeapMemoryUsage().getUsed());

            // 获取磁盘信息
            java.io.File[] roots = java.io.File.listRoots();
            List<Map<String, Object>> disks = new ArrayList<>();
            for (java.io.File root : roots) {
                Map<String, Object> disk = new HashMap<>();
                disk.put("path", root.getAbsolutePath());
                disk.put("totalSpace", root.getTotalSpace());
                disk.put("freeSpace", root.getFreeSpace());
                disk.put("usableSpace", root.getUsableSpace());
                disks.add(disk);
            }
            result.put("disks", disks);

            // 检查是否触发告警
            checkAlerts("cpuLoad", (double) result.get("cpuLoad"));
            checkAlerts("heapMemoryUsage", (long) result.get("heapMemoryUsage"));

            // 记录历史数据
            recordMetricData("cpuLoad", (double) result.get("cpuLoad"));
            recordMetricData("heapMemoryUsage", (long) result.get("heapMemoryUsage"));

            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取系统资源使用情况失败: " + e.getMessage());
        }

        return result;
    }
    
    /**
     * 告警规则内部类
     */
    private static class AlertRule {
        private String metricName;    // 指标名称
        private double threshold;     // 阈值
        private String operator;      // 比较运算符（>, <, >=, <=, ==, !=）
        private String alertLevel;    // 告警级别（INFO, WARNING, ERROR, CRITICAL）
        
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
    }
    
    /**
     * 指标数据内部类
     */
    private static class MetricData {
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

    @Override
    public List<Map<String, Object>> getDataProcessingTaskStatus() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 从Redis中获取任务状态信息
            Set<String> taskKeys = redisTemplate.keys("task:processing:*");
            if (taskKeys != null) {
                for (String key : taskKeys) {
                    Object taskObj = redisTemplate.opsForValue().get(key);
                    if (taskObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> taskStatus = (Map<String, Object>) taskObj;
                        result.add(taskStatus);
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取数据处理任务状态失败: " + e.getMessage());
            result.add(error);
        }

        return result;
    }

    @Override
    public Map<String, Object> getDataStorageStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取HDFS存储信息
            Map<String, Object> hdfsStatus = dataStorageService.getHdfsStatus();
            result.put("hdfs", hdfsStatus);

            // 获取HBase存储信息
            Map<String, Object> hbaseStatus = dataStorageService.getHbaseStatus();
            result.put("hbase", hbaseStatus);

            // 获取Elasticsearch存储信息
            Map<String, Object> esStatus = dataStorageService.getElasticsearchStatus();
            result.put("elasticsearch", esStatus);

            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据存储状态失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getDataIntegrationStatus() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 从Redis中获取数据接入任务状态
            Set<String> taskKeys = redisTemplate.keys("task:integration:*");
            if (taskKeys != null) {
                for (String key : taskKeys) {
                    Object taskObj = redisTemplate.opsForValue().get(key);
                    if (taskObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> taskStatus = (Map<String, Object>) taskObj;
                        result.add(taskStatus);
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取数据接入状态失败: " + e.getMessage());
            result.add(error);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAlerts(String startTime, String endTime) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 从Redis中获取告警信息
            Set<String> alertKeys = redisTemplate.keys("alert:*");
            if (alertKeys != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date start = sdf.parse(startTime);
                Date end = sdf.parse(endTime);

                for (String key : alertKeys) {
                    Object alertObj = redisTemplate.opsForValue().get(key);
                    if (alertObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> alert = (Map<String, Object>) alertObj;
                        if (alert.containsKey("timestamp")) {
                            Date alertTime = sdf.parse((String) alert.get("timestamp"));
                            if (alertTime.after(start) && alertTime.before(end)) {
                                result.add(alert);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取告警信息失败: " + e.getMessage());
            result.add(error);
        }

        return result;
    }
    

    
    /**
     * 比较值与阈值
     * @param value 实际值
     * @param threshold 阈值
     * @param operator 比较运算符
     * @return 是否触发告警
     */

    
    /**
     * 记录指标数据
     * @param metricName 指标名称
     * @param value 指标值
     */

            
            // 添加到历史数据列表
            history.add(data);
            
            // 限制历史数据数量，只保留最近的1000条记录
            if (history.size() > 1000) {
                history = history.subList(history.size() - 1000, history.size());
                metricHistory.put(metricName, history);
            }
        } catch (Exception e) {
            // 记录异常但不影响主流程
            System.err.println("记录指标数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 告警规则内部类
     */
    private static class AlertRule {
        private String metricName;    // 指标名称
        private double threshold;     // 阈值
        private String operator;      // 比较运算符（>, <, >=, <=, ==, !=）
        private String alertLevel;    // 告警级别（INFO, WARNING, ERROR, CRITICAL）
        
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
    }
    
    /**
     * 指标数据内部类
     */
    private static class MetricData {
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

    @Override
    public Map<String, Object> setAlertRule(String metricName, double threshold, String operator, String alertLevel) {
        Map<String, Object> result = new HashMap<>();

        try {
            AlertRule rule = new AlertRule(metricName, threshold, operator, alertLevel);
            alertRules.put(metricName, rule);

            // 保存到Redis
            redisTemplate.opsForValue().set("alertRule:" + metricName, rule);

            result.put("success", true);
            result.put("message", "告警规则设置成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置告警规则失败: " + e.getMessage());
        }

        return result;
    }
    

    
    /**
     * 比较值与阈值
     * @param value 实际值
     * @param threshold 阈值
     * @param operator 比较运算符
     * @return 是否触发告警
     */

    
    /**
     * 记录指标数据
     * @param metricName 指标名称
     * @param value 指标值
     */

            
            // 添加到历史数据列表
            history.add(data);
            
            // 限制历史数据数量，只保留最近的1000条记录
            if (history.size() > 1000) {
                history = history.subList(history.size() - 1000, history.size());
                metricHistory.put(metricName, history);
            }
        } catch (Exception e) {
            // 记录异常但不影响主流程
            System.err.println("记录指标数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 告警规则内部类
     */
    private static class AlertRule {
        private String metricName;    // 指标名称
        private double threshold;     // 阈值
        private String operator;      // 比较运算符（>, <, >=, <=, ==, !=）
        private String alertLevel;    // 告警级别（INFO, WARNING, ERROR, CRITICAL）
        
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
    }
    
    /**
     * 指标数据内部类
     */
    private static class MetricData {
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

    @Override
    public Map<String, Object> getMetricHistory(String metricName, String startTime, String endTime, String interval) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<MetricData> history = metricHistory.getOrDefault(metricName, new ArrayList<>());
            List<Map<String, Object>> filteredHistory = new ArrayList<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);

            // 过滤时间范围内的数据
            for (MetricData data : history) {
                if (data.getTimestamp().after(start) && data.getTimestamp().before(end)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("timestamp", sdf.format(data.getTimestamp()));
                    item.put("value", data.getValue());
                    filteredHistory.add(item);
                }
            }
            
            result.put("data", filteredHistory);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取指标历史数据失败: " + e.getMessage());
        }

        return result;
    }
}