package com.bigdataai.security.dto;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 */
public class LoginRequest {

    @NotBlank(message = "用户名或邮箱不能为空")
    private String usernameOrEmail;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 获取用户名或邮箱
     * @return 用户名或邮箱
     */
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    /**
     * 设置用户名或邮箱
     * @param usernameOrEmail 用户名或邮箱
     */
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    /**
     * 获取密码
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}