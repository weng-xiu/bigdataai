package com.bigdataai.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限实体类
 */
@Data
@TableName("permissions")
public class Permission {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    /**
     * 获取权限名称
     * @return 权限名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置权限名称
     * @param name 权限名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    private String description;

    /**
     * 获取权限描述
     * @return 权限描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置权限描述
     * @param description 权限描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    // 权限类型：菜单、按钮、API等
    private String type;

    /**
     * 获取权限类型
     * @return 权限类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置权限类型
     * @param type 权限类型
     */
    public void setType(String type) {
        this.type = type;
    }
    
    // 权限标识，如：user:create, user:update等
    private String permission;

    // Explicit Getters to ensure compilation
    /**
     * 获取权限ID
     * @return 权限ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置权限ID
     * @param id 权限ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取权限标识
     * @return 权限标识
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 设置权限标识
     * @param permission 权限标识
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }
}