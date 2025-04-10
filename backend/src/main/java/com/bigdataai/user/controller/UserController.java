package com.bigdataai.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bigdataai.user.model.User;
import com.bigdataai.user.model.UserLog;
import com.bigdataai.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 * 提供用户的CRUD操作、分页查询、批量删除等功能
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param user 用户信息
     * @param roleName 角色名称
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, @RequestParam String roleName) {
        try {
            User registeredUser = userService.registerUser(user, roleName);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 登录功能已移至AuthController

    /**
     * 获取用户列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键字
     * @return 用户列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        IPage<User> userPage = userService.findUsersByPage(page, size, keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userPage.getRecords());
        response.put("total", userPage.getTotal());
        response.put("pages", userPage.getPages());
        response.put("current", userPage.getCurrent());
        response.put("size", userPage.getSize());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(u -> ResponseEntity.ok(u))
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "用户不存在");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 修改密码
     * @param id 用户ID
     * @param passwordRequest 密码请求
     * @return 修改结果
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('USER_UPDATE') or #id == authentication.principal.id")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordRequest) {
        
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");
        
        boolean success = userService.changePassword(id, oldPassword, newPassword);
        
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "旧密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 为用户分配角色
     * @param id 用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<?> assignRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        
        try {
            User user = userService.assignRoles(id, roleIds);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<?> batchDeleteUsers(@RequestBody List<Long> ids) {
        try {
            userService.batchDeleteUsers(ids);
            Map<String, String> response = new HashMap<>();
            response.put("message", "批量删除用户成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 解锁用户账户
     * @param id 用户ID
     * @return 解锁结果
     */
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        boolean success = userService.unlockUser(id);
        
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "用户账户解锁成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "用户不存在或账户未锁定");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户登录日志
     * @param id 用户ID
     * @param limit 限制数量
     * @return 登录日志列表
     */
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('USER_READ') or #id == authentication.principal.id")
    public ResponseEntity<?> getUserLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<UserLog> logs = userService.getUserLoginLogs(id, limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据关键字搜索用户
     * @param keyword 关键字
     * @return 用户列表
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.findUsersByKeyword(keyword);
        return ResponseEntity.ok(users);
    }
}