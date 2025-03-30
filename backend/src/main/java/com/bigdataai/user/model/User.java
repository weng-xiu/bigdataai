package com.bigdataai.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private String realName;
    
    private Date createTime;
    
    private Date lastLoginTime;
    
    private Boolean enabled = true;
    
    /**
     * 用户角色关系需要通过中间表手动管理
     * 使用@TableField(exist=false)标记非数据库字段
     */
    @TableField(exist = false)
    private Set<Role> roles = new HashSet<>();
}