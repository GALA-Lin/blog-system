package com.blog.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-13:36
 * @Description:
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色信息
     * @param code 角色编码
     * @return 角色信息
     */
    @Select("SELECT * FROM roles WHERE code = #{code} LIMIT 1")
    Role selectByCode(@Param("code") String code);
}
