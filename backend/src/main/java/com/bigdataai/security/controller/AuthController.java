package com.bigdataai.security.controller;

import com.bigdataai.security.CustomUserDetailsService;
import com.bigdataai.security.JwtTokenUtil;
import com.bigdataai.user.model.User;
import com.bigdataai.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证控制器
 * 处理用户登录和令牌刷新请求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param authRequest 登录请求
     * @return JWT令牌
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> authRequest) {
        String username = authRequest.get("username");
        String password = authRequest.get("password");
        
        try {
            authenticate(username, password);
            
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            final String token = jwtTokenUtil.generateToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            Optional<User> userOpt = userService.findByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            userOpt.ifPresent(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("roles", user.getRoles());
                response.put("user", userInfo);
            });
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 刷新令牌
     * @param refreshTokenRequest 刷新令牌请求
     * @return 新的JWT令牌
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.get("refreshToken");
        
        try {
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            
            if (!jwtTokenUtil.canTokenBeRefreshed(refreshToken)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "刷新令牌已过期");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            final String newToken = jwtTokenUtil.refreshToken(refreshToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "刷新令牌无效");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 验证用户凭证
     * @param username 用户名
     * @param password 密码
     * @throws Exception 认证异常
     */
    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("用户已禁用", e);
        } catch (BadCredentialsException e) {
            throw new Exception("用户名或密码错误", e);
        }
    }
}