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
     * 登录失败次数
     */
    private Integer loginFailCount = 0;
    
    /**
     * 账户是否锁定
     */
    private Boolean locked = false;
    
    /**
     * 账户锁定时间
     */
    private Date lockedTime;
    
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
        return this.enabled != null && this.enabled && !isLocked();
    }
    
    /**
     * 判断用户是否锁定
     * @return 是否锁定
     */
    public boolean isLocked() {
        return this.locked != null && this.locked;
    }
    
    /**
     * 增加登录失败次数
     */
    public void incrementLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 1;
        } else {
            this.loginFailCount++;
        }
    }
    
    /**
     * 重置登录失败次数
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }
    
    /**
     * 锁定账户
     */
    public void lock() {
        this.locked = true;
        this.lockedTime = new Date();
    }
    
    /**
     * 解锁账户
     */
    public void unlock() {
        this.locked = false;
        this.lockedTime = null;
        resetLoginFailCount();
    }
}