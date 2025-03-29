package com.bigdataai.user.service;

import com.bigdataai.user.model.Role;

import java.util.List;
import java.util.Optional;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建后的角色信息
     */
    Role createRole(Role role);

    /**
     * 根据ID查找角色
     * @param id 角色ID
     * @return 角色信息
     */
    Optional<Role> findById(Long id);

    /**
     * 根据角色名称查找角色
     * @param name 角色名称
     * @return 角色信息
     */
    Optional<Role> findByName(String name);

    /**
     * 查找所有角色
     * @return 角色列表
     */
    List<Role> findAll();

    /**
     * 更新角色信息
     * @param role 角色信息
     * @return 更新后的角色信息
     */
    Role updateRole(Role role);

    /**
     * 删除角色
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 更新后的角色信息
     */
    Role assignPermissions(Long roleId, List<Long> permissionIds);
}