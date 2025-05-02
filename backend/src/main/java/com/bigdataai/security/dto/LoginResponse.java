package com.bigdataai.security.dto;

/**
 * 登录响应 DTO
 */
public class LoginResponse {

    private String token;

    /**
     * 构造函数
     * @param token JWT 令牌
     */
    public LoginResponse(String token) {
        this.token = token;
    }

    /**
     * 获取 JWT 令牌
     * @return JWT 令牌
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置 JWT 令牌
     * @param token JWT 令牌
     */
    public void setToken(String token) {
        this.token = token;
    }
}