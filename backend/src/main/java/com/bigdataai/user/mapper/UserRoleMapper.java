package com.bigdataai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface UserRoleMapper {
    
    /**
     * 为用户添加角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 删除用户的所有角色
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);
    
    /**
     * 获取用户的所有角色ID
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 检查用户是否有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否有角色
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int existsUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}