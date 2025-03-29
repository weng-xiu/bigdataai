package com.bigdataai.user.repository;

import com.bigdataai.user.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 权限数据访问接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * 根据权限名称查找权限
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
}