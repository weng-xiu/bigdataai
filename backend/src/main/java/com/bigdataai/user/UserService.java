package com.bigdataai.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    public void registerUser(String username, String password, String role) {
        // 用户注册逻辑
    }
    
    public boolean loginUser(String username, String password) {
        // 用户登录逻辑
        return true;
    }
    
    public boolean checkPermission(String username, String permission) {
        // 权限检查逻辑
        return true;
    }
}