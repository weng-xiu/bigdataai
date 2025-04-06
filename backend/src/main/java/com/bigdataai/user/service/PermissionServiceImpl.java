package com.bigdataai.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdataai.user.mapper.PermissionMapper;
import com.bigdataai.user.model.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 权限服务实现类
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    @Transactional
    public Permission createPermission(Permission permission) {
        // 检查权限名是否已存在
        Permission existingPermission = permissionMapper.selectOne(
            new LambdaQueryWrapper<Permission>().eq(Permission::getName, permission.getName())
        );
        if (existingPermission != null) {
            throw new RuntimeException("权限名已存在");
        }
        
        // 检查权限标识是否已存在
        if (permission.getPermission() != null) {
            Permission existingPermByCode = permissionMapper.selectOne(
                new LambdaQueryWrapper<Permission>().eq(Permission::getPermission, permission.getPermission())
            );
            if (existingPermByCode != null) {
                throw new RuntimeException("权限标识已存在");
            }
        }
        
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return Optional.ofNullable(permissionMapper.selectById(id));
    }
    
    @Override
    public Optional<Permission> findByName(String name) {
        return Optional.ofNullable(permissionMapper.selectOne(
            new LambdaQueryWrapper<Permission>().eq(Permission::getName, name)
        ));
    }

    @Override
    public Optional<Permission> findByPermission(String permission) {
        return Optional.ofNullable(permissionMapper.selectOne(
            new LambdaQueryWrapper<Permission>().eq(Permission::getPermission, permission)
        ));
    }

    @Override
    public List<Permission> findAll() {
        return permissionMapper.selectList(null);
    }

    @Override
    @Transactional
    public Permission updatePermission(Permission permission) {
        // 检查权限是否存在
        Permission existingPermission = permissionMapper.selectById(permission.getId());
        if (existingPermission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 检查权限名是否与其他权限重复
        Permission permByName = permissionMapper.selectOne(
            new LambdaQueryWrapper<Permission>()
                .eq(Permission::getName, permission.getName())
                .ne(Permission::getId, permission.getId())
        );
        if (permByName != null) {
            throw new RuntimeException("权限名已存在");
        }
        
        // 检查权限标识是否与其他权限重复
        if (permission.getPermission() != null) {
            Permission permByCode = permissionMapper.selectOne(
                new LambdaQueryWrapper<Permission>()
                    .eq(Permission::getPermission, permission.getPermission())
                    .ne(Permission::getId, permission.getId())
            );
            if (permByCode != null) {
                throw new RuntimeException("权限标识已存在");
            }
        }
        
        permissionMapper.updateById(permission);
        return permission;
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        // 检查权限是否存在
        Permission existingPermission = permissionMapper.selectById(id);
        if (existingPermission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // TODO: 检查权限是否被角色使用，如果被使用则不能删除
        
        permissionMapper.deleteById(id);
    }
}