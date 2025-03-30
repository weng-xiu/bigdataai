package com.bigdataai.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * JWT认证入口点
 * 当用户尝试访问安全资源但未提供有效凭证时，此类将处理异常并返回401错误
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -7858869558953243875L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 发送未授权错误，状态码401
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未授权：认证令牌无效或已过期");
    }
}