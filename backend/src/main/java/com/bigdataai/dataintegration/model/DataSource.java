package com.bigdataai.dataintegration.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 数据源实体类
 */
@Data
@TableName("data_source")
public class DataSource {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 获取数据源ID
     * @return 数据源ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据源ID
     * @param id 数据源ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    private String name;

    /**
     * 获取数据源名称
     * @return 数据源名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置数据源名称
     * @param name 数据源名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    private DataSourceType type;
    
    private String connectionUrl;
    
    private String username;
    
    private String password;
    
    private String description;
    
    @TableField(exist = false)
    private Map<String, String> properties;
    
    private Boolean enabled = true;
    
    private Date createTime;

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    private Date updateTime;

    // Explicit Getters to ensure compilation
    public DataSourceType getType() {
        return type;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    // Explicit Setters to fix compilation errors
    /**
     * 设置数据源类型
     * @param type 数据源类型
     */
    public void setType(DataSourceType type) { // Renamed from setDataSourceType
        this.type = type;
    }

    /**
     * 设置连接 URL
     * @param connectionUrl 连接 URL
     */
    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    // Explicit Setters to ensure compilation
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    // Explicit Setters to ensure compilation
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}