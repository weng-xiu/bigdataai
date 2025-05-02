package com.bigdataai.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体类
 */
@Data
@TableName("roles")
public class Role {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 获取角色ID
     * @return 角色ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置角色ID
     * @param id 角色ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    private String name;

    /**
     * 获取角色名称
     * @return 角色名称
     */
    public String getName() {
        return name;
    }
    
    private String description;

    /**
     * 获取角色描述
     * @return 角色描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 角色权限关系需要通过中间表手动管理
     * 使用@TableField(exist=false)标记非数据库字段
     */
    @TableField(exist = false)
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 获取角色权限集合
     * @return 角色权限集合
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * 设置角色权限集合
     * @param permissions 角色权限集合
     */
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}