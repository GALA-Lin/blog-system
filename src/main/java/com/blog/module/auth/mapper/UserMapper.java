package com.blog.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-01:10
 * @Description:
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 验证用户名是否存在
     * @param username 用户名
     * @return true: 存在, false: 不存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 验证邮箱是否存在
     * @param email 邮箱
     * @return true: 存在, false: 不存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);

    /**
     * 根据用户名查询用户及其角色和权限
     * @param username 用户名
     * @return 用户及其角色和权限
    */
    @Select("""
        SELECT u.*,
               GROUP_CONCAT(DISTINCT r.code) as role_codes,
               GROUP_CONCAT(DISTINCT p.code) as permission_codes
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        LEFT JOIN role_permissions rp ON r.id = rp.role_id
        LEFT JOIN permissions p ON rp.permission_id = p.id
        WHERE u.username = #{username}
        GROUP BY u.id
    """)
    User selectUserWithAuthorities(@Param("username") String username);


    /**
     * 获取用户的权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("""
        SELECT DISTINCT p.code
        FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        INNER JOIN user_roles ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId}
    """)
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("""
        SELECT DISTINCT r.code
        FROM roles r
        INNER JOIN user_roles ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId}
    """)
    List<String> selectRolesByUserId(@Param("userId") Long userId);
}