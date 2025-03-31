package com.bigdataai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdataai.user.model.UserLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 用户操作日志Mapper接口
 */
@Mapper
public interface UserLogMapper extends BaseMapper<UserLog> {
    
    /**
     * 查询用户最近的登录日志
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 日志列表
     */
    @Select("SELECT * FROM user_logs WHERE user_id = #{userId} AND operation_type = 'LOGIN' ORDER BY operation_time DESC LIMIT #{limit}")
    List<UserLog> selectRecentLoginLogs(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 查询用户在指定时间段内的登录失败次数
     * @param username 用户名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM user_logs WHERE username = #{username} AND operation_type = 'LOGIN' AND status = 'FAILURE' AND operation_time BETWEEN #{startTime} AND #{endTime}")
    int countLoginFailures(@Param("username") String username, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 查询指定IP在时间段内的登录失败次数
     * @param ipAddress IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM user_logs WHERE ip_address = #{ipAddress} AND operation_type = 'LOGIN' AND status = 'FAILURE' AND operation_time BETWEEN #{startTime} AND #{endTime}")
    int countLoginFailuresByIp(@Param("ipAddress") String ipAddress, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}