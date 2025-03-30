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
    
    private String fullName;
    
    private Date createTime;
    
    private Date lastLoginTime;
    
    private Boolean enabled = true;
    
    /**
     * 用户角色关系需要通过中间表手动管理
     * 使用@TableField(exist=false)标记非数据库字段
     */
    @TableField(exist = false)
    private Set<Role> roles = new HashSet<>();
    
    /**
     * 添加角色到用户
     * @param role 角色
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    /**
     * 判断用户是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return this.enabled != null && this.enabled;
    }
}