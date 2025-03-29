package com.bigdataai.user.service;

import com.bigdataai.user.model.Permission;

import java.util.List;
import java.util.Optional;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建后的权限信息
     */
    Permission createPermission(Permission permission);

    /**
     * 根据ID查找权限
     * @param id 权限ID
     * @return 权限信息
     */
    Optional<Permission> findById(Long id);

    /**
     * 根据权限名称查找权限
     * @param name 权限名称
     * @return 权限信息
     */
    Optional<Permission> findByName(String name);

    /**
     * 根据权限标识查找权限
     * @param permission 权限标识
     * @return 权限信息
     */
    Optional<Permission> findByPermission(String permission);

    /**
     * 查找所有权限
     * @return 权限列表
     */
    List<Permission> findAll();

    /**
     * 更新权限信息
     * @param permission 权限信息
     * @return 更新后的权限信息
     */
    Permission updatePermission(Permission permission);

    /**
     * 删除权限
     * @param id 权限ID
     */
    void deletePermission(Long id);
}