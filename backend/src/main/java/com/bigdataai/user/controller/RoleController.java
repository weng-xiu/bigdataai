package com.bigdataai.user.controller;

import com.bigdataai.user.model.Role;
import com.bigdataai.user.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        try {
            Role createdRole = roleService.createRole(role);
            return ResponseEntity.ok(createdRole);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有角色
     * @return 角色列表
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll().stream()
                .map(role -> {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId().toString());
                    roleMap.put("name", role.getName());
                    // Role类中没有getCode方法，移除此行
                    roleMap.put("description", role.getDescription());
                    return roleMap;
                })
                .collect(Collectors.toList()));
    }

    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleService.findById(id);
        return role.map(r -> {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put("id", r.getId().toString());
                    roleMap.put("name", r.getName());
                    roleMap.put("description", r.getDescription());
                    return ResponseEntity.ok(roleMap);
                })
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "角色不存在");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    /**
     * 更新角色信息
     * @param id 角色ID
     * @param role 角色信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            role.setId(id);
            Role updatedRole = roleService.updateRole(role);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "角色删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 为角色分配权限
     * @param id 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配结果
     */
    @PostMapping("/{id}/permissions")
    public ResponseEntity<?> assignPermissions(
            @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        try {
            roleService.assignPermissions(id, permissionIds);
            Map<String, String> response = new HashMap<>();
            response.put("message", "权限分配成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}