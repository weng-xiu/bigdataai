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
    
    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Long getId() {
        return id;
    }
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private String fullName;
    
    /**
     * 获取用户全名
     * @return 用户全名
     */
    public String getFullName() {
        return fullName;
    }
    
    private Date createTime;
    
    /**
     * 获取创建时间
     * @return 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }
    
    private Date lastLoginTime;
    
    /**
     * 设置最后登录时间
     * @param lastLoginTime 最后登录时间
     */
    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
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
    private Date lockTime;
    
    @TableField(exist = false)
    private Set<String> roleNames = new HashSet<>(); // Renamed from roles to roleNames
    
    @TableField(exist = false)
    private Set<String> permissions = new HashSet<>();

    // Explicit Getters and Setters to fix compilation errors
    /**
     * 获取密码
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 设置创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 设置登录失败次数
     * @param loginFailCount 登录失败次数
     */
    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    /**
     * 设置账户是否锁定
     * @param locked 是否锁定
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    /**
     * 获取账户锁定时间
     * @return 账户锁定时间
     */
    public Date getLockedTime() {
        return lockTime;
    }
    
    /**
     * 用户角色关系需要通过中间表手动管理
     * 使用@TableField(exist=false)标记非数据库字段
     */
    @TableField(exist = false)
    private Set<Role> roles = new HashSet<>();

    /**
     * 获取用户角色集合
     * @return 用户角色集合
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * 设置用户角色集合
     * @param roles 用户角色集合
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
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

    // Explicit Getters to ensure compilation
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
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