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
    
    private String name;
    
    private DataSourceType type;
    
    private String connectionUrl;
    
    private String username;
    
    private String password;
    
    private String description;
    
    @TableField(exist = false)
    private Map<String, String> properties;
    
    private Boolean enabled = true;
    
    private Date createTime;
    
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