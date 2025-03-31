package com.bigdataai.user.service;

import com.bigdataai.user.model.User;
import com.bigdataai.user.model.UserLog;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param user 用户信息
     * @param roleName 角色名称
     * @return 注册后的用户信息
     */
    User registerUser(User user, String roleName);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 登录用户信息
     */
    Optional<User> login(String username, String password, String ipAddress, String userAgent);

    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户信息
     */
    Optional<User> findById(Long id);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 查找所有用户
     * @return 用户列表
     */
    List<User> findAllUsers();

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(User user);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 修改密码
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 更新后的用户信息
     */
    User assignRoles(Long userId, List<Long> roleIds);

    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);
    
    /**
     * 解锁用户账户
     * @param userId 用户ID
     * @return 是否成功解锁
     */
    boolean unlockUser(Long userId);
    
    /**
     * 获取用户登录日志
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 日志列表
     */
    List<UserLog> getUserLoginLogs(Long userId, int limit);
}