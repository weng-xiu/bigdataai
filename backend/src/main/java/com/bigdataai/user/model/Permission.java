package com.bigdataai.user.model;

import lombok.Data;

import javax.persistence.*;

/**
 * 权限实体类
 */
@Data
@Entity
@Table(name = "permissions")
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    // 权限类型：菜单、按钮、API等
    private String type;
    
    // 权限标识，如：user:create, user:update等
    private String permission;
}