package com.bigdataai.user.controller;

import com.bigdataai.user.model.User;
import com.bigdataai.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
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
     * 获取所有用户
     * @return 用户列表
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findByUsername(id.toString());
        return user.map(ResponseEntity::ok)
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
}