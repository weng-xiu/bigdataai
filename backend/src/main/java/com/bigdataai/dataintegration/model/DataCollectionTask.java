package com.bigdataai.dataintegration.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

/**
 * 数据采集任务实体类
 */
@Data
@Entity
@Table(name = "data_collection_task")
public class DataCollectionTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String taskType; // FULL_IMPORT, INCREMENTAL_SYNC
    
    @ManyToOne
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;
    
    @Column(nullable = false)
    private String sourceTable; // 源表名/集合名/主题名/文件路径
    
    @Column(nullable = false)
    private String targetPath; // 目标存储路径
    
    @Column(columnDefinition = "TEXT")
    private String queryCondition; // 查询条件，JSON格式
    
    private String cronExpression; // 定时表达式
    
    @ElementCollection
    @CollectionTable(name = "data_collection_task_properties", joinColumns = @JoinColumn(name = "task_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    private Map<String, String> properties; // 任务属性
    
    @Column(columnDefinition = "TEXT")
    private String description; // 任务描述
    
    @Column(nullable = false)
    private String status; // PENDING, RUNNING, COMPLETED, FAILED
    
    private Date lastExecutionTime; // 最后执行时间
    
    @Column(nullable = false)
    private Boolean enabled = true; // 是否启用
    
    @Column(nullable = false)
    private Date createTime; // 创建时间
    
    @Column(nullable = false)
    private Date updateTime; // 更新时间
}