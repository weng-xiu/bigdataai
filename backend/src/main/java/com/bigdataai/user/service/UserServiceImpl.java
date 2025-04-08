package com.bigdataai.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.Map;
import java.util.Collections;
import java.util.Objects;
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

    @Transactional
    public User registerUser(User user) {
        return registerUser(user, "ROLE_USER");
    }
    
    @Override
    @Transactional
    public User registerUser(User user, String roleName) {
        // 使用条件构造器检查用户名和邮箱是否已存在
        LambdaQueryWrapper<User> usernameQuery = Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        LambdaQueryWrapper<User> emailQuery = Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, user.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
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
        // 使用条件构造器查询用户，支持用户名或邮箱登录
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)
                .or()
                .eq(User::getEmail, username);
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            // 记录登录失败日志
            UserLog log = UserLog.createLoginFailureLog(username, ipAddress, userAgent, "用户名或邮箱不存在");
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
        // 使用条件构造器查询用户
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            loadUserRoles(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        // 使用条件构造器查询用户
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, email);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            loadUserRoles(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        // 使用条件构造器查询用户
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, usernameOrEmail)
                .or()
                .eq(User::getEmail, usernameOrEmail);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            loadUserRoles(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        // 使用条件构造器查询所有用户，并按创建时间降序排序
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .orderByDesc(User::getCreateTime);
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            loadUserRoles(user);
        }
        return users;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByKeyword(String keyword) {
        // 使用条件构造器进行模糊查询
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .or()
                .like(StringUtils.hasText(keyword), User::getEmail, keyword)
                .or()
                .like(StringUtils.hasText(keyword), User::getFullName, keyword)
                .orderByDesc(User::getCreateTime);
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            loadUserRoles(user);
        }
        return users;
    }
    
    @Override
    @Transactional(readOnly = true)
    public IPage<User> findUsersByPage(int pageNum, int pageSize, String keyword) {
        // 创建分页对象
        IPage<User> page = new Page<>(pageNum, pageSize);
        // 使用Mapper中的分页查询方法
        IPage<User> userPage = userMapper.selectUserPage(page, keyword);
        // 加载用户角色
        for (User user : userPage.getRecords()) {
            loadUserRoles(user);
        }
        return userPage;
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
    public void batchDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量删除用户角色关联
        for (Long id : ids) {
            userRoleMapper.deleteUserRoles(id);
        }
        // 批量删除用户
        userMapper.deleteBatchIds(ids);
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

        // 使用条件构造器检查用户名是否已被其他用户使用
        LambdaQueryWrapper<User> usernameQuery = Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .ne(User::getId, user.getId());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new IllegalArgumentException("用户名已被使用");
        }
        
        // 使用条件构造器检查邮箱是否已被其他用户使用
        LambdaQueryWrapper<User> emailQuery = Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, user.getEmail())
                .ne(User::getId, user.getId());
        if (userMapper.selectCount(emailQuery) > 0) {
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
            // 加载角色的权限信息
            for (Role role : roles) {
                List<Permission> permissions = roleMapper.selectPermissionsByRoleId(role.getId());
                role.setPermissions(new HashSet<>(permissions));
            }
            user.setRoles(new HashSet<>(roles));
        }
    }
    
    /**
     * 批量加载用户角色
     * @param users 用户列表
     */
    private void batchLoadUserRoles(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        
        // 提取所有用户ID
        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        
        // 批量查询用户角色关系
        Map<Long, List<Long>> userRoleMap = userRoleMapper.selectBatchUserRoles(userIds);
        
        // 提取所有角色ID
        List<Long> allRoleIds = userRoleMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询角色信息
        List<Role> allRoles = roleMapper.selectBatchIds(allRoleIds);
        Map<Long, Role> roleMap = allRoles.stream()
                .collect(Collectors.toMap(Role::getId, role -> role));
        
        // 为每个用户设置角色
        for (User user : users) {
            List<Long> roleIds = userRoleMap.getOrDefault(user.getId(), Collections.emptyList());
            Set<Role> userRoles = roleIds.stream()
                    .map(roleMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(userRoles);
        }
    }
}