package com.bigdataai.security.controller;

import com.bigdataai.common.ApiResponse; // Added import
import com.bigdataai.security.CustomUserDetailsService;
import com.bigdataai.security.JwtTokenUtil;
import com.bigdataai.user.model.Permission;
import com.bigdataai.user.model.Role;
import com.bigdataai.user.model.User;
import com.bigdataai.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.LockedException; // Added import
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException; // Added import
import org.springframework.security.core.GrantedAuthority; // Added import
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Added import
import org.slf4j.LoggerFactory; // Added import

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 处理用户注册、登录、令牌刷新和验证请求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class); // Added logger

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
     * 生成验证码
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    @GetMapping(value = "/captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    public void generateCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        
        // 生成验证码
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // 设置边框
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
        
        // 添加干扰线
        Random random = new Random();
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        
        // 生成随机验证码
        String captchaCode = generateCaptchaCode(4);
        
        // 将验证码存入session
        HttpSession session = request.getSession();
        session.setAttribute("captchaCode", captchaCode);
        session.setAttribute("captchaExpireTime", System.currentTimeMillis() + 5 * 60 * 1000); // 5分钟有效期
        
        // 绘制验证码
        g.setColor(new Color(19, 148, 246));
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString(captchaCode, 12, 30);
        g.dispose();
        
        // 输出图像
        ImageIO.write(image, "JPEG", response.getOutputStream());
    }
    
    /**
     * 生成随机验证码
     * @param length 验证码长度
     * @return 验证码字符串
     */
    private String generateCaptchaCode(int length) {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * 验证验证码
     * @param captchaRequest 验证码请求
     * @param request HTTP请求
     * @return 验证结果
     */
    @PostMapping("/validate-captcha")
    public ResponseEntity<?> validateCaptcha(@RequestBody Map<String, String> captchaRequest, HttpServletRequest request) {
        String captcha = captchaRequest.get("captcha");
        HttpSession session = request.getSession();
        String sessionCaptcha = (String) session.getAttribute("captchaCode");
        Long captchaExpireTime = (Long) session.getAttribute("captchaExpireTime");
        
        Map<String, Object> response = new HashMap<>();
        
        // 验证码为空或已过期
        if (sessionCaptcha == null || captchaExpireTime == null || System.currentTimeMillis() > captchaExpireTime) {
            response.put("status", "error");
            response.put("message", "验证码已过期，请重新获取");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // 验证码不匹配
        if (!sessionCaptcha.equalsIgnoreCase(captcha)) {
            response.put("status", "error");
            response.put("message", "验证码错误");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // 验证成功，清除session中的验证码
        session.removeAttribute("captchaCode");
        session.removeAttribute("captchaExpireTime");
        
        response.put("status", "success");
        response.put("message", "验证码验证成功");
        return ResponseEntity.ok(response);
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
                                  @RequestHeader(value = "X-Real-IP", required = false) String realIp,
                                  HttpServletRequest request) {
        String usernameOrEmail = authRequest.get("username");
        String password = authRequest.get("password");
        String captcha = authRequest.get("captcha");
        
        Map<String, Object> response = new HashMap<>();

        // 参数验证
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "用户名/邮箱和密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        HttpSession session = request.getSession();
        String sessionKeyPrefix = "loginFailCount_" + usernameOrEmail.toLowerCase(); // Use consistent key
        Integer loginFailCount = (Integer) session.getAttribute(sessionKeyPrefix);
        if (loginFailCount == null) {
            loginFailCount = 0;
        }

        // 验证码校验 (如果登录失败次数达到阈值)
        final int MAX_LOGIN_ATTEMPTS = 3; // 可配置
        if (loginFailCount >= MAX_LOGIN_ATTEMPTS) {
            if (captcha == null || captcha.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "请输入验证码");
                return ResponseEntity.badRequest().body(response);
            }

            String sessionCaptcha = (String) session.getAttribute("captchaCode");
            Long captchaExpireTime = (Long) session.getAttribute("captchaExpireTime");

            if (sessionCaptcha == null || captchaExpireTime == null || System.currentTimeMillis() > captchaExpireTime) {
                response.put("status", "error");
                response.put("message", "验证码已过期，请刷新");
                // 清除旧验证码，避免重复使用
                session.removeAttribute("captchaCode");
                session.removeAttribute("captchaExpireTime");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (!sessionCaptcha.equalsIgnoreCase(captcha)) {
                // 验证码错误也增加失败计数
                loginFailCount++;
                session.setAttribute(sessionKeyPrefix, loginFailCount);
                response.put("status", "error");
                response.put("message", "验证码错误");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            // 验证码正确，从session中移除，防止重复使用
            session.removeAttribute("captchaCode");
            session.removeAttribute("captchaExpireTime");
        }

        try {
            // 根据用户名或邮箱查找实际用户名
            String actualUsername = userService.findByUsernameOrEmail(usernameOrEmail) // Corrected method name
                    .map(User::getUsername) // Assuming User object is returned
                    .orElseThrow(() -> new BadCredentialsException("用户名或密码错误"));

            // 使用实际用户名进行认证
            authenticate(actualUsername, password);

            // 认证成功，加载用户信息并生成令牌
            final UserDetails userDetails = userDetailsService.loadUserByUsername(actualUsername);
            final String token = jwtTokenUtil.generateToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // 登录成功，重置失败计数器
            session.removeAttribute(sessionKeyPrefix);

            // 获取用户信息
            User user = userService.findByUsername(actualUsername).orElse(null); // 理论上此时用户一定存在

            // 构建成功响应
            response.put("status", "success");
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                // 注意：避免直接暴露密码等敏感信息
                // userInfo.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
                userInfo.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                response.put("user", userInfo);
            }

            // 记录登录日志 (可选)
            // logLoginAttempt(actualUsername, request, true, null);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // 登录失败，增加失败计数
            incrementLoginAttempts(session, sessionKeyPrefix); // Added method call
            // logLoginAttempt(usernameOrEmail, request, false, "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("用户名或密码错误"));
        } catch (DisabledException e) {
            // logLoginAttempt(usernameOrEmail, request, false, "用户已禁用");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("用户已禁用"));
        } catch (LockedException e) { // Catch specific LockedException
            // logLoginAttempt(usernameOrEmail, request, false, "账户已锁定");
            return ResponseEntity.status(HttpStatus.LOCKED).body(ApiResponse.error("账户已锁定，请稍后再试或联系管理员"));
        } catch (AuthenticationException e) { // Catch specific AuthenticationException
            // logLoginAttempt(usernameOrEmail, request, false, "认证失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("认证失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("登录时发生未知错误", e); // Use logger
            // logLoginAttempt(usernameOrEmail, request, false, "登录时发生未知错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("登录时发生未知错误，请联系管理员"));
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
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "刷新令牌不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            
            if (!jwtTokenUtil.canTokenBeRefreshed(refreshToken)) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "刷新令牌已过期");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 获取用户信息
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "用户不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userOpt.get();
            
            // 检查用户状态
            if (!user.isEnabled()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "账户已被禁用");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            if (user.isLocked()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "账户已被锁定");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 准备额外的声明信息
            Map<String, Object> additionalClaims = new HashMap<>();
            additionalClaims.put("userId", user.getId());
            additionalClaims.put("email", user.getEmail());
            
            // 添加角色信息
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            additionalClaims.put("roles", roles);
            
            // 添加权限信息 - 使用Set去重
            Set<String> permissionSet = new HashSet<>();
            for (Role role : user.getRoles()) {
                for (Permission permission : role.getPermissions()) {
                    permissionSet.add(permission.getPermission());
                }
            }
            additionalClaims.put("permissions", new ArrayList<>(permissionSet));
            additionalClaims.put("lastLoginTime", user.getLastLoginTime().getTime());
            
            // 生成新令牌
            final String newToken = jwtTokenUtil.generateToken(userDetails, additionalClaims);
            // 生成新的刷新令牌，增强安全性
            final String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "令牌刷新成功");
            response.put("token", newToken);
            response.put("refreshToken", newRefreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenUtil.getExpirationDateFromToken(newToken).getTime() / 1000);
            
            // 构建用户信息响应
            Map<String, Object> userInfo = buildUserInfoResponse(user);
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "刷新令牌无效: " + e.getMessage());
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
                    
                    // 添加权限信息 - 使用Set去重
                    Set<String> permissionSet = new HashSet<>();
                    for (Role role : user.getRoles()) {
                        for (Permission permission : role.getPermissions()) {
                            permissionSet.add(permission.getPermission());
                        }
                    }
                    userInfo.put("permissions", new ArrayList<>(permissionSet));
                    
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
            throw new DisabledException("用户已禁用", e); // 保持原始异常类型
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("用户名或密码错误", e); // 保持原始异常类型
        } catch (LockedException e) { // Added catch block for LockedException
            throw new LockedException("账户已锁定", e);
        } catch (AuthenticationException e) {
             // 捕获更具体的 AuthenticationException
             throw new Exception("认证失败: " + e.getMessage(), e);
        } catch (Exception e) {
            // 其他未知异常
            throw new Exception("认证过程中发生未知错误", e);
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


// 添加 incrementLoginAttempts 方法
private void incrementLoginAttempts(HttpSession session, String keyPrefix) {
    Integer attempts = (Integer) session.getAttribute(keyPrefix);
    if (attempts == null) {
        attempts = 0;
    }
    session.setAttribute(keyPrefix, attempts + 1);
}