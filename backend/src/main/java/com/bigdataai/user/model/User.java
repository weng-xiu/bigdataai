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
     * 设置用户ID
     * @param id 用户ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Long getId() {
        return id;
    }
    
    private String username;
    
    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    private String password;

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
    
    private String email;
    
    /**
     * 获取邮箱
     * @return 邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱
     * @param email 邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    private String phone;

    /**
     * 获取电话
     * @return 电话
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置电话
     * @param phone 电话
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    private String fullName;

    /**
     * 设置用户全名
     * @param fullName 用户全名
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
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

    /**
     * 设置创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    private Date lastLoginTime;

    /**
     * 获取最后登录时间
     * @return 最后登录时间
     */
    public Date getLastLoginTime() {
        return lastLoginTime;
    }
    
    /**
     * 设置最后登录时间
     * @param lastLoginTime 最后登录时间
     */
    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    private Boolean enabled = true;

    /**
     * 获取是否启用
     * @return 是否启用
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用
     * @param enabled 是否启用
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 登录失败次数
     */
    private Integer loginFailCount = 0;

    /**
     * 获取登录失败次数
     * @return 登录失败次数
     */
    public Integer getLoginFailCount() {
        return loginFailCount;
    }

    /**
     * 设置登录失败次数
     * @param loginFailCount 登录失败次数
     */
    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    /**
     * 账户是否锁定
     */
    private Boolean locked = false;

    /**
     * 获取账户是否锁定
     * @return 是否锁定
     */
    public Boolean getLocked() {
        return locked;
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
     * 设置账户锁定时间
     * @param lockTime 账户锁定时间
     */
    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }
    
    /**
     * 账户锁定时间
     */
    private Date lockTime;

    @TableField(exist = false)
    private Set<Role> roles = new HashSet<>();

    /**
     * 获取用户角色
     * @return 用户角色集合
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * 设置用户角色
     * @param roles 用户角色集合
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @TableField(exist = false)
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 获取用户权限
     * @return 用户权限集合
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * 设置用户权限
     * @param permissions 用户权限集合
     */
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
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