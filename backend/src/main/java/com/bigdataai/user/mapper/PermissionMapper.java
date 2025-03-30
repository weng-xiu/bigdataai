package com.bigdataai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdataai.user.model.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
}