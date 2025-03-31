package com.bigdataai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdataai.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户Mapper接口
 * 继承MyBatis-Plus的BaseMapper接口，获取基础的CRUD功能
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(@Param("email") String email);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int existsByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int existsByEmail(@Param("email") String email);
    
    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     * @return 影响行数
     */
    @Select("UPDATE users SET last_login_time = #{lastLoginTime} WHERE id = #{userId}")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") java.util.Date lastLoginTime);
    
    /**
     * 更新用户登录失败次数
     * @param userId 用户ID
     * @param loginFailCount 登录失败次数
     * @return 影响行数
     */
    @Select("UPDATE users SET login_fail_count = #{loginFailCount} WHERE id = #{userId}")
    int updateLoginFailCount(@Param("userId") Long userId, @Param("loginFailCount") Integer loginFailCount);
    
    /**
     * 锁定用户账户
     * @param userId 用户ID
     * @param locked 是否锁定
     * @return 影响行数
     */
    @Select("UPDATE users SET locked = #{locked} WHERE id = #{userId}")
    int updateLockedStatus(@Param("userId") Long userId, @Param("locked") Boolean locked);
}