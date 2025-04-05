package com.bigdataai.security;

import com.bigdataai.user.model.Permission;
import com.bigdataai.user.model.Role;
import com.bigdataai.user.model.User;
import com.bigdataai.user.mapper.UserMapper;
import com.bigdataai.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用MyBatis-Plus的UserMapper替代JPA的UserRepository
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在: " + username);
        }
        
        // 加载用户角色
        Optional<User> userWithRoles = userService.findByUsername(username);
        if (userWithRoles.isPresent()) {
            user = userWithRoles.get();
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                !user.isLocked(),  // 账户未锁定
                getAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
        return getGrantedAuthorities(getPermissions(roles));
    }

    private List<String> getPermissions(Collection<Role> roles) {
        List<String> permissions = new ArrayList<>();
        List<Permission> collection = new ArrayList<>();
        
        for (Role role : roles) {
            permissions.add(role.getName());
            collection.addAll(role.getPermissions());
        }
        
        for (Permission permission : collection) {
            permissions.add(permission.getPermission());
        }
        
        return permissions;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        
        return authorities;
    }
}