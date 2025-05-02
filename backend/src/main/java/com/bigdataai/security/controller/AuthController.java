package com.bigdataai.security.controller;

import com.bigdataai.security.dto.AuthRequest;
import com.bigdataai.security.dto.AuthResponse;
import com.bigdataai.security.dto.RefreshTokenRequest;
// import com.bigdataai.security.jwt.JwtTokenProvider; // Replaced by JwtTokenUtil
// import com.bigdataai.security.service.RefreshTokenService; // Refresh token logic might be elsewhere or handled by JwtTokenUtil
import com.bigdataai.user.model.User;
import com.bigdataai.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Add missing import
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // Add missing import
import java.util.HashMap;
import java.util.Map;

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
        String ipAddress = getClientIpAddress(request, forwardedIp, realIp);
        
        // 检查是否需要验证码 (基于Session中的失败记录或用户状态，此处简化，先尝试认证)
        // boolean requiresCaptcha = checkCaptchaRequirement(usernameOrEmail, session);
        boolean requiresCaptchaCheckPostFailure = true; // 标记是否需要在认证失败后检查验证码需求
        boolean captchaVerified = false; // 标记验证码是否已验证（如果需要的话）

        // 尝试从数据库获取用户信息，以判断是否一开始就需要验证码（例如账户已锁定）
        Optional<User> initialUserOptional = userService.findByUsernameOrEmail(usernameOrEmail);
        if (initialUserOptional.isPresent()) {
            User initialUser = initialUserOptional.get();
            if (initialUser.isLocked() || initialUser.getLoginFailCount() >= 3) { // 假设失败3次或已锁定则需要验证码
                String sessionCaptcha = (String) session.getAttribute("captchaCode");
                Long captchaExpireTime = (Long) session.getAttribute("captchaExpireTime");

                if (captcha == null || captcha.trim().isEmpty()) {
                    response.put("status", "error");
                    response.put("message", "请输入验证码");
                    response.put("captchaRequired", true);
                    return ResponseEntity.badRequest().body(response);
                }
                if (sessionCaptcha == null || captchaExpireTime == null || System.currentTimeMillis() > captchaExpireTime) {
                    response.put("status", "error");
                    response.put("message", "验证码已过期，请重新获取");
                    response.put("captchaRequired", true);
                    return ResponseEntity.badRequest().body(response);
                }
                if (!sessionCaptcha.equalsIgnoreCase(captcha)) {
                    response.put("status", "error");
                    response.put("message", "验证码错误");
                    response.put("captchaRequired", true);
                    return ResponseEntity.badRequest().body(response);
                }
                // 验证码验证成功
                session.removeAttribute("captchaCode");
                session.removeAttribute("captchaExpireTime");
                captchaVerified = true;
                requiresCaptchaCheckPostFailure = false; // 初始验证码已通过，失败后无需再次检查
            }
        }

        try {
            // 使用用户名进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取认证成功的用户信息
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // 使用 UserServiceImpl 的 login 方法处理成功逻辑（重置计数、记录日志等）
            Optional<User> loggedInUserOptional = userService.login(userDetails.getUsername(), password, ipAddress, userAgent);

            if (!loggedInUserOptional.isPresent()) {
                 // login 方法内部已处理失败情况，理论上认证成功后这里应该总是有用户
                 // 但为保险起见，添加错误处理
                 log.error("Login successful via AuthenticationManager but UserServiceImpl.login failed for {}", userDetails.getUsername());
                 response.put("status", "error");
                 response.put("message", "登录处理失败，请联系管理员");
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            User loggedInUser = loggedInUserOptional.get();
            
            // UserServiceImpl.login 已记录成功日志，此处无需重复记录
            // UserLog log = UserLog.createLoginSuccessLog(loggedInUser.getId(), loggedInUser.getUsername(), ipAddress, userAgent);
            // userService.saveUserLog(log); 

            // 生成令牌
            final String token = jwtTokenUtil.generateToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // 构建成功响应
            response.put("status", "success");
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            response.put("message", "登录成功");
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", loggedInUser.getId());
            userInfo.put("username", loggedInUser.getUsername());
            userInfo.put("email", loggedInUser.getEmail());
            // 确保返回的是角色名称列表
            userInfo.put("roles", loggedInUser.getRoles() != null ? 
                                    loggedInUser.getRoles().stream().map(Role::getName).collect(Collectors.toList()) : 
                                    Collections.emptyList()); 
            response.put("user", userInfo);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // 密码错误 - UserServiceImpl.login 已处理失败计数和日志
            response.put("status", "error");
            response.put("message", "用户名或密码错误");
            // 认证失败后检查是否需要验证码
            checkAndSetCaptchaRequired(usernameOrEmail, response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (LockedException e) {
            // 账户锁定 - UserServiceImpl.login 已处理日志
            response.put("status", "error");
            response.put("message", "账户已锁定");
            response.put("captchaRequired", true); // 账户锁定时强制要求验证码
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (DisabledException e) {
            // 账户禁用 - UserServiceImpl.login 已处理日志
            response.put("status", "error");
            response.put("message", "账户已禁用");
            // 禁用的账户通常不需要验证码，但可以根据策略调整
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (UsernameNotFoundException e) {
            // 用户名不存在 - UserServiceImpl.login 已处理日志
            response.put("status", "error");
            response.put("message", "用户名或邮箱不存在");
            // 用户不存在时通常不需要验证码
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (AuthenticationException e) {
            // 其他认证异常
            log.error("Authentication failed for user {}: {}", usernameOrEmail, e.getMessage());
            response.put("status", "error");
            response.put("message", "认证失败: " + e.getMessage());
            // 认证失败后检查是否需要验证码
            checkAndSetCaptchaRequired(usernameOrEmail, response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            // 其他未知错误
            log.error("Login error for user {}: {}", usernameOrEmail, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "登录过程中发生内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查用户状态并设置 captchaRequired 标志
     * @param usernameOrEmail 用户名或邮箱
     * @param response 响应Map
     */
    private void checkAndSetCaptchaRequired(String usernameOrEmail, Map<String, Object> response) {
        Optional<User> userOptional = userService.findByUsernameOrEmail(usernameOrEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 根据失败次数决定是否需要验证码 (假设失败3次后需要)
            if (user.getLoginFailCount() >= 3) { 
                response.put("captchaRequired", true);
            }
        }
        // 如果用户不存在，则不设置 captchaRequired
    }

    /**
     * 获取客户端IP地址
     * @param request HTTP请求
     * @param forwardedIp X-Forwarded-For头
     * @param realIp X-Real-IP头
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request, String forwardedIp, String realIp) {
        String ipAddress = realIp; // 优先使用 X-Real-IP
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = forwardedIp; // 其次使用 X-Forwarded-For
        }
        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            // X-Forwarded-For 可能包含多个IP，取第一个
            int commaIndex = ipAddress.indexOf(',');
            if (commaIndex != -1) {
                ipAddress = ipAddress.substring(0, commaIndex);
            }
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr(); // 最后使用 request.getRemoteAddr()
        }
        return ipAddress;
    }

    // 移除 handleLoginFailure 方法，逻辑已整合到 catch 块和 checkAndSetCaptchaRequired
    /*
    private void handleLoginFailure(String usernameOrEmail, String ipAddress, String userAgent, String reason, Map<String, Object> response) {
        ...
    }
    */

    /**
     * 检查用户状态并设置 captchaRequired 标志




    // 移除重复的 getClientIpAddress 方法
    /*
    private void handleLoginFailure(String usernameOrEmail, String ipAddress, String userAgent, String reason, Map<String, Object> response) {
        ...
    }
    */
}