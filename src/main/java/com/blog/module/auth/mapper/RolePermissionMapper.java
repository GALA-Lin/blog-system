package com.blog.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-13:47
 * @Description:
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    // 继承BaseMapper接口
}
