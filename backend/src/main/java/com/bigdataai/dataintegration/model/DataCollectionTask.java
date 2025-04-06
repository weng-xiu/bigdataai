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
    
    private String name;
    
    private String taskType; // FULL_IMPORT, INCREMENTAL_SYNC
    
    private Long dataSourceId;
    
    private String sourceTable; // 源表名/集合名/主题名/文件路径
    
    private String targetPath; // 目标存储路径
    
    private String queryCondition; // 查询条件，JSON格式
    
    private String cronExpression; // 定时表达式
    
    @TableField(exist = false)
    private Map<String, String> properties; // 任务属性
    
    private String description; // 任务描述
    
    private String status; // PENDING, RUNNING, COMPLETED, FAILED
    
    private Date lastExecutionTime; // 最后执行时间
    
    private Boolean enabled = true; // 是否启用
    
    private Date createTime; // 创建时间
    
    private Date updateTime; // 更新时间
}