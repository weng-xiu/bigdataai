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
    
    private String name;
    
    private String description;
    
    /**
     * 角色权限关系需要通过中间表手动管理
     * 使用@TableField(exist=false)标记非数据库字段
     */
    @TableField(exist = false)
    private Set<Permission> permissions = new HashSet<>();
}