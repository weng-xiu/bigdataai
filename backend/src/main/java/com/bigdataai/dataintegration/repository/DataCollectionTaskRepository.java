package com.bigdataai.dataintegration.repository;

import com.bigdataai.dataintegration.model.DataCollectionTask;
import java.util.List;
import java.util.Optional;

/**
 * 数据采集任务仓库接口
 * 注意：这是一个临时接口，应该被替换为MyBatis-Plus的Mapper接口
 */
public interface DataCollectionTaskRepository {
    
    /**
     * 保存数据采集任务
     * @param task 数据采集任务
     * @return 保存后的数据采集任务
     */
    DataCollectionTask save(DataCollectionTask task);
    
    /**
     * 根据ID查找数据采集任务
     * @param id 任务ID
     * @return 数据采集任务
     */
    Optional<DataCollectionTask> findById(Long id);
    
    /**
     * 查找所有数据采集任务
     * @return 数据采集任务列表
     */
    List<DataCollectionTask> findAll();
    
    /**
     * 根据数据源ID查找数据采集任务
     * @param dataSourceId 数据源ID
     * @return 数据采集任务列表
     */
    List<DataCollectionTask> findByDataSourceId(Long dataSourceId);
    
    /**
     * 删除数据采集任务
     * @param task 数据采集任务
     */
    void delete(DataCollectionTask task);
    
    /**
     * 根据ID删除数据采集任务
     * @param id 任务ID
     */
    void deleteById(Long id);
}