package com.blog.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-13:41
 * @Description:
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    // 继承了BaseMapper，所以不需要写其他方法
}
