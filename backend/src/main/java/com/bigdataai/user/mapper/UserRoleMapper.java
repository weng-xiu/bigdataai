package com.bigdataai.user.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface UserRoleMapper {
    
    /**
     * 添加用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 批量添加用户角色关联
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 影响行数
     */
    @Insert({"<script>",
            "INSERT INTO user_roles (user_id, role_id) VALUES ",
            "<foreach collection='roleIds' item='roleId' separator=','>",
            "(#{userId}, #{roleId})",
            "</foreach>",
            "</script>"})
    int batchInsertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    
    /**
     * 删除用户的所有角色关联
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);
    
    /**
     * 批量删除用户角色关联
     * @param userIds 用户ID列表
     * @return 影响行数
     */
    @Delete({"<script>",
            "DELETE FROM user_roles WHERE user_id IN ",
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>",
            "#{userId}",
            "</foreach>",
            "</script>"})
    int batchDeleteUserRoles(@Param("userIds") List<Long> userIds);
    
    /**
     * 查询用户的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 批量查询用户的角色ID列表
     * @param userIds 用户ID列表
     * @return 用户ID到角色ID列表的映射
     */
    @Select({"<script>",
            "SELECT user_id, role_id FROM user_roles WHERE user_id IN ",
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>",
            "#{userId}",
            "</foreach>",
            "</script>"})
    List<Map<String, Object>> selectBatchUserRolesRaw(@Param("userIds") List<Long> userIds);
    
    /**
     * 将原始查询结果转换为用户ID到角色ID列表的映射
     * @param userIds 用户ID列表
     * @return 用户ID到角色ID列表的映射
     */
    default Map<Long, List<Long>> selectBatchUserRoles(List<Long> userIds) {
        List<Map<String, Object>> rawResults = selectBatchUserRolesRaw(userIds);
        Map<Long, List<Long>> result = new java.util.HashMap<>();
        
        for (Map<String, Object> row : rawResults) {
            Long userId = ((Number) row.get("user_id")).longValue();
            Long roleId = ((Number) row.get("role_id")).longValue();
            
            result.computeIfAbsent(userId, k -> new java.util.ArrayList<>()).add(roleId);
        }
        
        return result;
    }
    
    /**
     * 检查用户是否有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否有角色
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int existsUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}