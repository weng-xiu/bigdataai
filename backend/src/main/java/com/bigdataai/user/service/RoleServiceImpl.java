package com.bigdataai.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bigdataai.user.mapper.RoleMapper;
import com.bigdataai.user.model.Permission;
import com.bigdataai.user.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色服务实现类
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Role createRole(Role role) {
        // 检查角色名称是否已存在
        LambdaQueryWrapper<Role> queryWrapper = Wrappers.<Role>lambdaQuery()
                .eq(Role::getName, role.getName());
        if (roleMapper.selectCount(queryWrapper) > 0) {
            throw new IllegalArgumentException("角色名称已存在");
        }

        // 注意：Role 实体类中没有 createTime 和 updateTime 字段
        
        // 保存角色
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role != null) {
            loadRolePermissions(role);
            return Optional.of(role);
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        LambdaQueryWrapper<Role> queryWrapper = Wrappers.<Role>lambdaQuery()
                .eq(Role::getName, name);
        Role role = roleMapper.selectOne(queryWrapper);
        if (role != null) {
            loadRolePermissions(role);
            return Optional.of(role);
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        List<Role> roles = roleMapper.selectList(null);
        for (Role role : roles) {
            loadRolePermissions(role);
        }
        return roles;
    }

    @Override
    @Transactional
    public Role updateRole(Role role) {
        Role existingRole = roleMapper.selectById(role.getId());
        if (existingRole == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        // 检查角色名称是否已被其他角色使用
        LambdaQueryWrapper<Role> queryWrapper = Wrappers.<Role>lambdaQuery()
                .eq(Role::getName, role.getName())
                .ne(Role::getId, role.getId());
        if (roleMapper.selectCount(queryWrapper) > 0) {
            throw new IllegalArgumentException("角色名称已存在");
        }

        // 更新角色信息
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        
        roleMapper.updateById(existingRole);
        
        // 重新加载权限信息
        loadRolePermissions(existingRole);
        return existingRole;
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        // 先删除角色权限关联
        jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", id);
        
        // 删除角色
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Role assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        // 先删除原有权限关联
        jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
        
        // 添加新权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                jdbcTemplate.update("INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)", roleId, permissionId);
            }
        }
        
        // 重新加载权限信息
        loadRolePermissions(role);
        return role;
    }

    /**
     * 加载角色权限信息
     * @param role 角色
     */
    private void loadRolePermissions(Role role) {
        List<Permission> permissions = roleMapper.selectPermissionsByRoleId(role.getId());
        role.setPermissions(new HashSet<>(permissions));
    }
}