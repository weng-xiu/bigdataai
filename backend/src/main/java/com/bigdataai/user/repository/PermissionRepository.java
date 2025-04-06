package com.bigdataai.user.repository;

import com.bigdataai.user.model.Permission;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓库接口
 * 注意：这是一个临时接口，应该被替换为MyBatis-Plus的Mapper接口
 */
public interface PermissionRepository {
    
    /**
     * 保存权限
     * @param permission 权限对象
     * @return 保存后的权限对象
     */
    Permission save(Permission permission);
    
    /**
     * 根据ID查找权限
     * @param id 权限ID
     * @return 权限对象
     */
    Optional<Permission> findById(Long id);
    
    /**
     * 根据名称查找权限
     * @param name 权限名称
     * @return 权限对象
     */
    Optional<Permission> findByName(String name);
    
    /**
     * 根据权限标识查找权限
     * @param permission 权限标识
     * @return 权限对象
     */
    Optional<Permission> findByPermission(String permission);
    
    /**
     * 查找所有权限
     * @return 权限列表
     */
    List<Permission> findAll();
    
    /**
     * 删除权限
     * @param permission 权限对象
     */
    void delete(Permission permission);
}