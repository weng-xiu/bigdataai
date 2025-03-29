package com.bigdataai.dataintegration.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

/**
 * 数据源实体类
 */
@Data
@Entity
@Table(name = "data_source")
public class DataSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DataSourceType type;
    
    @Column(nullable = false)
    private String connectionUrl;
    
    private String username;
    
    private String password;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "data_source_properties", joinColumns = @JoinColumn(name = "data_source_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    private Map<String, String> properties;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(nullable = false)
    private Date createTime;
    
    @Column(nullable = false)
    private Date updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
}