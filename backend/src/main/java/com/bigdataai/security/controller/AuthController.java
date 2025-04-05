package com.bigdataai.security.controller;

import com.bigdataai.security.CustomUserDetailsService;
import com.bigdataai.security.JwtTokenUtil;
import com.bigdataai.user.model.Permission;
import com.bigdataai.user.model.Role;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 处理用户注册、登录、令牌刷新和验证请求
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
     * 用户注册
     * @param user 用户信息
     * @param roleName 角色名称
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, @RequestParam(defaultValue = "ROLE_USER") String roleName) {
        try {
            // 检查用户名是否已存在
            if (userService.findByUsername(user.getUsername()).isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "用户名已存在");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // 注册用户
            User registeredUser = userService.registerUser(user, roleName);
            
            // 生成令牌
            final UserDetails userDetails = userDetailsService.loadUserByUsername(registeredUser.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            response.put("message", "注册成功");
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", registeredUser.getId());
            userInfo.put("username", registeredUser.getUsername());
            userInfo.put("email", registeredUser.getEmail());
            userInfo.put("roles", registeredUser.getRoles());
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户登录
     * @param authRequest 登录请求
     * @return JWT令牌
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> authRequest, 
                                  @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp,
                                  @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                  @RequestHeader(value = "X-Real-IP", required = false) String realIp) {
        String usernameOrEmail = authRequest.get("username");
        String password = authRequest.get("password");
        
        // 获取客户端IP地址
        String ipAddress = forwardedIp;
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = realIp;
        }
        
        try {
            // 使用UserService的login方法，支持用户名或邮箱登录，记录登录日志和处理账户锁定
            Optional<User> userOpt = userService.login(usernameOrEmail, password, ipAddress, userAgent);
            
            if (!userOpt.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "用户名/邮箱或密码错误");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userOpt.get();
            
            // 生成JWT令牌
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            
            // 构建用户信息响应
            Map<String, Object> userInfo = buildUserInfoResponse(user);
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * 构建用户信息响应
     * @param user 用户对象
     * @return 用户信息Map
     */
    private Map<String, Object> buildUserInfoResponse(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("lastLoginTime", user.getLastLoginTime());
        userInfo.put("createTime", user.getCreateTime());
        userInfo.put("enabled", user.isEnabled());
        
        // 添加角色信息
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        userInfo.put("roles", roleNames);
        
        // 添加权限信息 - 使用Set去重
        Set<String> permissionSet = new HashSet<>();
        for (Role role : user.getRoles()) {
            for (Permission permission : role.getPermissions()) {
                permissionSet.add(permission.getPermission());
            }
        }
        userInfo.put("permissions", new ArrayList<>(permissionSet));
        
        return userInfo;
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
            // 生成新的刷新令牌，增强安全性
            final String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            Optional<User> userOpt = userService.findByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("refreshToken", newRefreshToken);
            
            userOpt.ifPresent(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                response.put("user", userInfo);
            });
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "刷新令牌无效");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * 验证令牌
     * @param tokenRequest 令牌请求
     * @return 验证结果
     */
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        String token = tokenRequest.get("token");
        
        try {
            // 从令牌中获取用户名
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 验证令牌有效性
            boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                response.put("username", username);
                Optional<User> userOpt = userService.findByUsername(username);
                userOpt.ifPresent(user -> {
                    // 添加用户基本信息
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("fullName", user.getFullName());
                    userInfo.put("lastLoginTime", user.getLastLoginTime());
                    
                    // 添加角色信息
                    List<String> roles = user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toList());
                    userInfo.put("roles", roles);
                    
                    // 添加权限信息
                    List<String> permissions = new ArrayList<>();
                    for (Role role : user.getRoles()) {
                        for (Permission permission : role.getPermissions()) {
                            permissions.add(permission.getPermission());
                        }
                    }
                    userInfo.put("permissions", permissions);
                    
                    response.put("user", userInfo);
                });
                
                // 添加令牌过期时间
                response.put("expiresAt", jwtTokenUtil.getExpirationDateFromToken(token));
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "令牌无效或已过期");
            return ResponseEntity.ok(response);
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
        } catch (Exception e) {
            throw new Exception("认证失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理认证异常
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAuthenticationException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
}