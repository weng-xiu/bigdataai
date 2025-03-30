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
    
    private String description;
    
    // 权限类型：菜单、按钮、API等
    private String type;
    
    // 权限标识，如：user:create, user:update等
    private String permission;
}