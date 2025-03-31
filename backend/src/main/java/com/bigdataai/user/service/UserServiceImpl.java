package com.bigdataai.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bigdataai.user.mapper.RoleMapper;
import com.bigdataai.user.mapper.UserLogMapper;
import com.bigdataai.user.mapper.UserMapper;
import com.bigdataai.user.mapper.UserRoleMapper;
import com.bigdataai.user.model.Permission;
import com.bigdataai.user.model.Role;
import com.bigdataai.user.model.User;
import com.bigdataai.user.model.UserLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private UserLogMapper userLogMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${security.login.max-fail-count:5}")
    private int maxLoginFailCount;
    
    @Value("${security.login.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    @Override
    @Transactional
    public User registerUser(User user) {
        return registerUser(user, "ROLE_USER");
    }
    
    @Override
    @Transactional
    public User registerUser(User user, String roleName) {
        // 检查用户名和邮箱是否已存在
        if (userMapper.existsByUsername(user.getUsername()) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userMapper.existsByEmail(user.getEmail()) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置创建时间
        user.setCreateTime(new Date());
        user.setLoginFailCount(0);
        user.setLocked(false);
        
        // 保存用户
        userMapper.insert(user);
        
        // 设置指定角色
        Role role = roleMapper.selectByName(roleName);
        if (role == null) {
            // 如果指定角色不存在，则使用默认角色
            role = roleMapper.selectByName("ROLE_USER");
            if (role == null) {
                throw new IllegalArgumentException("系统错误：默认角色不存在");
            }
        }
        
        // 添加用户角色关联
        userRoleMapper.insertUserRole(user.getId(), role.getId());
        
        // 设置角色到用户对象
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return user;
    }

    @Override
    @Transactional
    public Optional<User> login(String username, String password, String ipAddress, String userAgent) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            // 记录登录失败日志
            UserLog log = UserLog.createLoginFailureLog(username, ipAddress, userAgent, "用户名不存在");
            userLogMapper.insert(log);
            return Optional.empty();
        }
        
        // 检查账户是否锁定
        if (user.isLocked()) {
            // 检查锁定时间是否已过期
            if (user.getLockedTime() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(user.getLockedTime());
                calendar.add(Calendar.MINUTE, lockDurationMinutes);
                
                if (new Date().after(calendar.getTime())) {
                    // 锁定时间已过期，解锁账户
                    user.unlock();
                    userMapper.updateById(user);
                } else {
                    // 账户仍然锁定
                    UserLog log = UserLog.createLoginFailureLog(username, ipAddress, userAgent, "账户已锁定");
                    userLogMapper.insert(log);
                    return Optional.empty();
                }
            }
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 增加登录失败次数
            user.incrementLoginFailCount();
            
            // 检查是否达到最大失败次数
            if (user.getLoginFailCount() >= maxLoginFailCount) {
                user.lock();
            }
            
            userMapper.updateById(user);
            
            // 记录登录失败日志
            UserLog log = UserLog.createLoginFailureLog(username, ipAddress, userAgent, "密码错误");
            userLogMapper.insert(log);
            
            return Optional.empty();
        }
        
        // 登录成功，重置失败次数
        user.resetLoginFailCount();
        user.setLastLoginTime(new Date());
        userMapper.updateById(user);
        
        // 加载用户角色
        loadUserRoles(user);
        
        // 记录登录成功日志
        UserLog log = UserLog.createLoginSuccessLog(user.getId(), username, ipAddress, userAgent);
        userLogMapper.insert(log);
        
        return Optional.of(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            loadUserRoles(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            loadUserRoles(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            loadUserRoles(user);
        }
        return users;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // 先删除用户角色关联
        userRoleMapper.deleteUserRoles(id);
        // 再删除用户
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
                
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return true;
    }

    @Override
    @Transactional
    public User assignRoles(Long userId, List<Long> roleIds) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 先删除原有角色关联
        userRoleMapper.deleteUserRoles(userId);
        
        // 添加新角色关联
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                throw new IllegalArgumentException("角色不存在: " + roleId);
            }
            userRoleMapper.insertUserRole(userId, roleId);
            roles.add(role);
        }
        
        user.setRoles(roles);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permission) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        loadUserRoles(user);
        
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(p -> p.getPermission().equals(permission));
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查用户名和邮箱是否已被其他用户使用
        User userByUsername = userMapper.selectByUsername(user.getUsername());
        if (userByUsername != null && !userByUsername.getId().equals(user.getId())) {
            throw new IllegalArgumentException("用户名已被使用");
        }
        
        User userByEmail = userMapper.selectByEmail(user.getEmail());
        if (userByEmail != null && !userByEmail.getId().equals(user.getId())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
        
        // 更新用户信息
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFullName(user.getFullName());
        existingUser.setPhone(user.getPhone());
        existingUser.setEnabled(user.getEnabled());
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(user.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // 保存更新后的用户
        userMapper.updateById(existingUser);
        
        // 更新角色
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<Long> roleIds = user.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toList());
            assignRoles(user.getId(), roleIds);
        }
        
        // 重新加载用户角色
        loadUserRoles(existingUser);
        
        return existingUser;
    }
    
    @Override
    @Transactional
    public boolean unlockUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        if (user.isLocked()) {
            user.unlock();
            userMapper.updateById(user);
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserLog> getUserLoginLogs(Long userId, int limit) {
        return userLogMapper.selectRecentLoginLogs(userId, limit);
    }
    
    /**
     * 加载用户角色
     * @param user 用户
     */
    private void loadUserRoles(User user) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            user.setRoles(new HashSet<>(roles));
        }
    }
}