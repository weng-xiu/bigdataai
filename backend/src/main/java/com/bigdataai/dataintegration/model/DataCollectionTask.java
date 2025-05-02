package com.bigdataai.dataintegration.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 数据采集任务实体类
 */
@Data
@TableName("data_collection_task")
public class DataCollectionTask {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 获取任务ID
     * @return 任务ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置任务ID
     * @param id 任务ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    private String name;

    /**
     * 获取任务名称
     * @return 任务名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置任务名称
     * @param name 任务名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    private String taskType; // FULL_IMPORT, INCREMENTAL_SYNC

    /**
     * 获取任务类型
     * @return 任务类型
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * 设置任务类型
     * @param taskType 任务类型
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    private Long dataSourceId;

    /**
     * 设置数据源ID
     * @param dataSourceId 数据源ID
     */
    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
    
    private String sourceTable; // 源表名/集合名/主题名/文件路径
    
    /**
     * 设置源表名/集合名/主题名/文件路径
     * @param sourceTable 源表名/集合名/主题名/文件路径
     */
    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }
    
    private String targetPath; // 目标存储路径
    
    /**
     * 获取目标存储路径
     * @return 目标存储路径
     */
    public String getTargetPath() {
        return targetPath;
    }

    /**
     * 设置目标存储路径
     * @param targetPath 目标存储路径
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
    
    private String queryCondition; // 查询条件，JSON格式
    
    /**
     * 设置查询条件
     * @param queryCondition 查询条件
     */
    public void setQueryCondition(String queryCondition) {
        this.queryCondition = queryCondition;
    }
    
    private String cronExpression; // 定时表达式
    
    /**
     * 设置定时表达式
     * @param cronExpression 定时表达式
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    @TableField(exist = false)
    private Map<String, String> properties; // 任务属性
    
    /**
     * 设置任务属性
     * @param properties 任务属性
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    private String description; // 任务描述
    
    /**
     * 获取任务描述
     * @return 任务描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置任务描述
     * @param description 任务描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    private String status; // PENDING, RUNNING, COMPLETED, FAILED
    
    private Date lastExecutionTime; // 最后执行时间
    
    /**
     * 获取最后执行时间
     * @return 最后执行时间
     */
    public Date getLastExecutionTime() {
        return lastExecutionTime;
    }

    /**
     * 设置最后执行时间
     * @param lastExecutionTime 最后执行时间
     */
    public void setLastExecutionTime(Date lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
    }
    
    private Boolean enabled = true; // 是否启用
    
    /**
     * 设置是否启用
     * @param enabled 是否启用
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    private Date createTime; // 创建时间
    
    /**
     * 获取创建时间
     * @return 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }
    
    private Date updateTime; // 更新时间

    /**
     * 获取更新时间
     * @return 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    // Explicit Setters to ensure compilation
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Explicit Getters/Setters to ensure compilation
    public Long getDataSourceId() {
        return dataSourceId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setLastExecutionTime(Date lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getQueryCondition() {
        return queryCondition;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getStatus() {
        return status;
    }
}