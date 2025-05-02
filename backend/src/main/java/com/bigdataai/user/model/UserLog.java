package com.bigdataai.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户操作日志实体类
 */
@Data
@TableName("user_logs")
public class UserLog {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    /**
     * 用户名
     */
    private String username;

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * 操作类型：LOGIN, LOGOUT, REGISTER, PASSWORD_CHANGE, PROFILE_UPDATE, etc.
     */
    private String operationType;

    /**
     * 设置操作类型
     * @param operationType 操作类型
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    /**
     * 操作描述
     */
    private String description;

    /**
     * 设置操作描述
     * @param description 操作描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 操作IP地址
     */
    private String ipAddress;

    /**
     * 设置操作IP地址
     * @param ipAddress 操作IP地址
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    /**
     * 操作时间
     */
    private Date operationTime;

    /**
     * 设置操作时间
     * @param operationTime 操作时间
     */
    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }
    
    /**
     * 操作状态：SUCCESS, FAILURE
     */
    private String status;

    /**
     * 设置操作状态
     * @param status 操作状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 设置失败原因
     * @param failureReason 失败原因
     */
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    /**
     * 用户代理信息
     */
    private String userAgent;

    /**
     * 设置用户代理信息
     * @param userAgent 用户代理信息
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    /**
     * 创建成功的登录日志
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 日志对象
     */
    public static UserLog createLoginSuccessLog(Long userId, String username, String ipAddress, String userAgent) {
        UserLog log = new UserLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationType("LOGIN");
        log.setDescription("用户登录成功");
        log.setIpAddress(ipAddress);
        log.setOperationTime(new Date());
        log.setStatus("SUCCESS");
        log.setUserAgent(userAgent);
        return log;
    }
    
    /**
     * 创建失败的登录日志
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param reason 失败原因
     * @return 日志对象
     */
    public static UserLog createLoginFailureLog(String username, String ipAddress, String userAgent, String reason) {
        UserLog log = new UserLog();
        log.setUsername(username);
        log.setOperationType("LOGIN");
        log.setDescription("用户登录失败");
        log.setIpAddress(ipAddress);
        log.setOperationTime(new Date());
        log.setStatus("FAILURE");
        log.setFailureReason(reason);
        log.setUserAgent(userAgent);
        return log;
    }
}