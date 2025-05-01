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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}