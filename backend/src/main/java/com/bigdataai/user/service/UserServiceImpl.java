package com.bigdataai.user.service;

import com.bigdataai.user.model.Permission;
import com.bigdataai.user.model.Role;
import com.bigdataai.user.model.User;
import com.bigdataai.user.repository.PermissionRepository;
import com.bigdataai.user.repository.RoleRepository;
import com.bigdataai.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(User user) {
        // 检查用户名和邮箱是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认角色
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole != null) {
            user.addRole(userRole);
        }

        // 设置创建时间
        user.setCreateTime(new Date());

        // 保存用户
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            return Optional.empty();
        }
        
        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Optional.empty();
        }
        
        return userOptional;
    }

    @Override
    @Transactional
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
                
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public User assignRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
                
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
            roles.add(role);
        }
        
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean hasPermission(Long userId, String permission) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
                
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(p -> p.getPermission().equals(permission));
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        // 检查用户是否存在
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 检查用户名和邮箱是否已被其他用户使用
        userRepository.findByUsername(user.getUsername()).ifPresent(userByUsername -> {
            if (!userByUsername.getId().equals(user.getId())) {
                throw new IllegalArgumentException("用户名已被使用");
            }
        });
        
        User userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail != null && !userByEmail.getId().equals(user.getId())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
        
        // 更新用户信息
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFullName(user.getFullName());
        existingUser.setPhone(user.getPhone());
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(user.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // 更新角色
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            existingUser.setRoles(user.getRoles());
        }
        
        // 保存更新后的用户
        return userRepository.save(existingUser);
    }
}