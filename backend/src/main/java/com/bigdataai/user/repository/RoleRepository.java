package com.bigdataai.user.repository;

import com.bigdataai.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 角色仓库接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色名查找角色
     * @param name 角色名
     * @return 角色信息
     */
    Role findByName(String name);

    /**
     * 检查角色名是否存在
     * @param name 角色名
     * @return 是否存在
     */
    boolean existsByName(String name);
}