package com.bigdataai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 用于生成和验证JWT令牌
 */
@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    // 令牌有效期，默认24小时
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60;

    // 刷新令牌有效期，默认7天
    private static final long JWT_REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60;

    @Value("${jwt.secret:bigdataai_secret_key}")
    private String secret;

    /**
     * 从令牌中获取用户名
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取过期日期
     * @param token 令牌
     * @return 过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取指定声明
     * @param token 令牌
     * @param claimsResolver 声明解析器
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中获取所有声明
     * @param token 令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * 检查令牌是否已过期
     * @param token 令牌
     * @return 是否已过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 生成令牌
     * @param userDetails 用户详情
     * @return 令牌
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), JWT_TOKEN_VALIDITY);
    }

    /**
     * 生成刷新令牌
     * @param userDetails 用户详情
     * @return 刷新令牌
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), JWT_REFRESH_TOKEN_VALIDITY);
    }

    /**
     * 生成令牌的核心方法
     * @param claims 声明
     * @param subject 主题（用户名）
     * @param validity 有效期（秒）
     * @return 令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String subject, long validity) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 验证令牌
     * @param token 令牌
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 检查刷新令牌是否可以被刷新
     * @param token 刷新令牌
     * @return 是否可以刷新
     */
    public Boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 刷新令牌
     * @param token 旧令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + JWT_TOKEN_VALIDITY * 1000);

        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}