package com.bigdataai.user.service;

import com.bigdataai.user.model.Permission;
import com.bigdataai.user.repository.PermissionRepository;
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
    private PermissionRepository permissionRepository;

    @Override
    @Transactional
    public Permission createPermission(Permission permission) {
        // 检查权限名是否已存在
        Optional<Permission> existingPermission = permissionRepository.findByName(permission.getName());
        if (existingPermission.isPresent()) {
            throw new RuntimeException("权限名已存在");
        }
        
        // 检查权限标识是否已存在
        if (permission.getPermission() != null) {
            Optional<Permission> existingPermByCode = permissionRepository.findByPermission(permission.getPermission());
            if (existingPermByCode.isPresent()) {
                throw new RuntimeException("权限标识已存在");
            }
        }
        
        return permissionRepository.save(permission);
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }
    
    @Override
    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    @Override
    public Optional<Permission> findByPermission(String permission) {
        return permissionRepository.findByPermission(permission);
    }

    @Override
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Override
    @Transactional
    public Permission updatePermission(Permission permission) {
        // 检查权限是否存在
        Optional<Permission> existingPermission = permissionRepository.findById(permission.getId());
        if (!existingPermission.isPresent()) {
            throw new RuntimeException("权限不存在");
        }
        
        // 检查权限名是否与其他权限重复
        Optional<Permission> permByName = permissionRepository.findByName(permission.getName());
        if (permByName.isPresent() && !permByName.get().getId().equals(permission.getId())) {
            throw new RuntimeException("权限名已存在");
        }
        
        // 检查权限标识是否与其他权限重复
        if (permission.getPermission() != null) {
            Optional<Permission> permByCode = permissionRepository.findByPermission(permission.getPermission());
            if (permByCode.isPresent() && !permByCode.get().getId().equals(permission.getId())) {
                throw new RuntimeException("权限标识已存在");
            }
        }
        
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        // 检查权限是否存在
        Optional<Permission> existingPermission = permissionRepository.findById(id);
        if (!existingPermission.isPresent()) {
            throw new RuntimeException("权限不存在");
        }
        
        // TODO: 检查权限是否被角色使用，如果被使用则不能删除
        
        permissionRepository.deleteById(id);
    }
}