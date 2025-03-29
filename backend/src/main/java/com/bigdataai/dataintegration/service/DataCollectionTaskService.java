package com.bigdataai.dataintegration.service;

import com.bigdataai.dataintegration.model.DataCollectionTask;

import java.util.List;
import java.util.Map;

/**
 * 数据采集任务服务接口
 */
public interface DataCollectionTaskService {
    
    /**
     * 创建数据采集任务
     * @param task 任务对象
     * @return 创建后的任务对象
     */
    DataCollectionTask createTask(DataCollectionTask task);
    
    /**
     * 更新数据采集任务
     * @param task 任务对象
     * @return 更新后的任务对象
     */
    DataCollectionTask updateTask(DataCollectionTask task);
    
    /**
     * 删除数据采集任务
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);
    
    /**
     * 获取数据采集任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    DataCollectionTask getTask(Long taskId);
    
    /**
     * 获取所有数据采集任务
     * @return 任务列表
     */
    List<DataCollectionTask> getAllTasks();
    
    /**
     * 根据数据源获取数据采集任务
     * @param dataSourceId 数据源ID
     * @return 任务列表
     */
    List<DataCollectionTask> getTasksByDataSource(Long dataSourceId);
    
    /**
     * 执行数据采集任务
     * @param taskId 任务ID
     * @return 执行结果
     */
    Map<String, Object> executeTask(Long taskId);
    
    /**
     * 启动任务调度
     * @param taskId 任务ID
     * @return 是否启动成功
     */
    boolean startTaskSchedule(Long taskId);
    
    /**
     * 取消任务调度
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelTaskSchedule(Long taskId);
    
    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    Map<String, Object> getTaskStatus(Long taskId);
}