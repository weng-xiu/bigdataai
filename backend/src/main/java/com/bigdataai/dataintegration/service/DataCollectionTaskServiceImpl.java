package com.bigdataai.dataintegration.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdataai.dataintegration.mapper.DataCollectionTaskMapper;
import com.bigdataai.dataintegration.mapper.DataSourceMapper;
import com.bigdataai.dataintegration.model.DataCollectionTask;
import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.model.DataSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 数据采集任务服务实现类
 */
@Service
public class DataCollectionTaskServiceImpl implements DataCollectionTaskService {

    @Autowired
    private DataCollectionTaskMapper taskMapper;
    
    @Autowired
    private DataSourceService dataSourceService;
    
    @Autowired
    private DataCollectorService dataCollectorService;
    
    @Autowired
    private TaskScheduler taskScheduler;
    
    // 存储任务调度的Future，用于取消任务
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    // 存储任务执行状态
    private final Map<Long, Map<String, Object>> taskStatusMap = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public DataCollectionTask createTask(DataCollectionTask task) {
        // 设置创建时间和更新时间
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        
        // 设置初始状态
        task.setStatus("PENDING");
        
        taskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public DataCollectionTask updateTask(DataCollectionTask task) {
        // 检查任务是否存在
        DataCollectionTask existingTask = taskMapper.selectById(task.getId());
        if (existingTask == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        
        // 更新任务
        existingTask.setName(task.getName());
        existingTask.setTaskType(task.getTaskType());
        existingTask.setSourceTable(task.getSourceTable());
        existingTask.setTargetPath(task.getTargetPath());
        existingTask.setQueryCondition(task.getQueryCondition());
        existingTask.setCronExpression(task.getCronExpression());
        existingTask.setProperties(task.getProperties());
        existingTask.setDescription(task.getDescription());
        existingTask.setEnabled(task.getEnabled());
        existingTask.setUpdateTime(new Date());
        
        taskMapper.updateById(existingTask);
        return existingTask;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        // 检查任务是否存在
        if (taskMapper.selectById(taskId) == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        
        // 取消任务调度
        cancelTaskSchedule(taskId);
        
        // 删除任务
        taskMapper.deleteById(taskId);
    }

    @Override
    public DataCollectionTask getTask(Long taskId) {
        DataCollectionTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        return task;
    }

    @Override
    public List<DataCollectionTask> getAllTasks() {
        return taskMapper.selectList(null);
    }

    @Override
    public List<DataCollectionTask> getTasksByDataSource(Long dataSourceId) {
        // 检查数据源是否存在
        DataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在");
        }
        
        // 使用LambdaQueryWrapper查询
        LambdaQueryWrapper<DataCollectionTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataCollectionTask::getDataSourceId, dataSourceId);
        return taskMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, Object> executeTask(Long taskId) {
        // 获取任务
        DataCollectionTask task = getTask(taskId);
        
        // 检查任务是否启用
        if (!task.getEnabled()) {
            throw new IllegalStateException("任务未启用");
        }
        
        // 更新任务状态
        task.setStatus("RUNNING");
        task.setLastExecutionTime(new Date());
        taskMapper.updateById(task);
        
        // 初始化任务状态
        Map<String, Object> status = new HashMap<>();
        status.put("taskId", taskId);
        status.put("startTime", new Date());
        status.put("status", "RUNNING");
        status.put("progress", 0);
        status.put("message", "任务开始执行");
        
        // 存储任务状态
        taskStatusMap.put(taskId, status);
        
        try {
            // 根据数据源类型和任务类型执行不同的数据采集操作
            DataSource dataSource = dataSourceService.getDataSource(task.getDataSourceId());
            String sourceTable = task.getSourceTable();
            String targetPath = task.getTargetPath();
            String queryCondition = task.getQueryCondition();
            Map<String, String> properties = task.getProperties();
            
            int count = 0;
            DataSourceType sourceType = dataSource.getType();
            
            // 解析查询条件
            Map<String, Object> conditions = new HashMap<>();
            if (queryCondition != null && !queryCondition.isEmpty()) {
                // 这里简化处理，实际应该使用JSON解析
                // conditions = objectMapper.readValue(queryCondition, Map.class);
            }
            
            // 根据数据源类型执行不同的数据采集操作
            switch (sourceType) {
                case MYSQL:
                    count = dataCollectorService.collectFromMySQL(dataSource, sourceTable, conditions);
                    break;
                case MONGODB:
                    count = dataCollectorService.collectFromMongoDB(dataSource, sourceTable, queryCondition);
                    break;
                case KAFKA:
                    String consumerGroup = properties.getOrDefault("consumerGroup", "default-group");
                    count = dataCollectorService.collectFromKafka(dataSource, sourceTable, consumerGroup);
                    break;
                case HDFS:
                    String fileFormat = properties.getOrDefault("fileFormat", "csv");
                    count = dataCollectorService.collectFromHDFS(dataSource, sourceTable, fileFormat);
                    break;
                case FILE_SYSTEM:
                    fileFormat = properties.getOrDefault("fileFormat", "csv");
                    count = dataCollectorService.collectFromFileSystem(dataSource, sourceTable, fileFormat);
                    break;
                case ELASTICSEARCH:
                    count = dataCollectorService.collectFromElasticsearch(dataSource, sourceTable, queryCondition);
                    break;
                case HBASE:
                    // 从properties中获取必要的HBase参数
                    String familyName = properties.getOrDefault("familyName", "default");
                    String startRow = properties.getOrDefault("startRow", "");
                    String endRow = properties.getOrDefault("endRow", "");
                    count = dataCollectorService.collectFromHBase(dataSource, sourceTable, familyName, startRow, endRow);
                    break;
                case API:
                    // 将conditions转换为String类型的Map
                    Map<String, String> apiParams = new HashMap<>();
                    if (conditions != null) {
                        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                            apiParams.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
                        }
                    }
                    count = dataCollectorService.collectFromAPI(dataSource, sourceTable, apiParams);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
            }
            
            // 更新任务状态
            status.put("status", "COMPLETED");
            status.put("progress", 100);
            status.put("message", "任务执行完成，共采集 " + count + " 条数据");
            status.put("endTime", new Date());
            status.put("dataCount", count);
            
            // 更新任务
            task.setStatus("COMPLETED");
            taskMapper.updateById(task);
            
            return status;
        } catch (Exception e) {
            // 更新任务状态
            status.put("status", "FAILED");
            status.put("message", "任务执行失败: " + e.getMessage());
            status.put("endTime", new Date());
            status.put("error", e.getMessage());
            
            // 更新任务
            task.setStatus("FAILED");
            taskMapper.updateById(task);
            
            return status;
        }
    }
    
    @Override
    public boolean startTaskSchedule(Long taskId) {
        // 获取任务
        DataCollectionTask task = getTask(taskId);
        
        // 检查任务是否启用
        if (!task.getEnabled()) {
            throw new IllegalStateException("任务未启用");
        }
        
        // 检查是否已有调度
        if (scheduledTasks.containsKey(taskId)) {
            return false; // 任务已经在调度中
        }
        
        // 检查是否有定时表达式
        String cronExpression = task.getCronExpression();
        if (cronExpression == null || cronExpression.isEmpty()) {
            throw new IllegalStateException("任务没有定时表达式");
        }
        
        try {
            // 创建定时任务
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> executeTask(taskId),
                    new CronTrigger(cronExpression)
            );
            
            // 存储任务调度
            scheduledTasks.put(taskId, future);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("启动任务调度失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean cancelTaskSchedule(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(taskId);
            return true;
        }
        return false;
    }
    
    @Override
    public Map<String, Object> getTaskStatus(Long taskId) {
        // 获取任务状态
        Map<String, Object> status = taskStatusMap.get(taskId);
        if (status == null) {
            // 如果没有状态信息，则返回任务基本信息
            DataCollectionTask task = getTask(taskId);
            status = new HashMap<>();
            status.put("taskId", taskId);
            status.put("status", task.getStatus());
            status.put("lastExecutionTime", task.getLastExecutionTime());
        }
        return status;
    }
}