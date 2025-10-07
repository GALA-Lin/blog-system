package com.blog.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.User;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-15:23
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", username);
        // 根据用户名查询用户信息
//        User user = userMapper.selectUserWithAuthorities(username);
//        if (user == null) {
//            log.debug("用户 {} 不存在", username);
//            throw new UsernameNotFoundException("用户 " + username + " 不存在");
//        } else {
//            log.debug("用户 {} 加载成功", username);
//            return (UserDetails) user;
//        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.error("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        // 检查用户状态
        if (user.getStatus() != 1) {
            log.error("账号已被禁用: {}", username);
            throw new RuntimeException("账号已被禁用");
        }

        // 查询用户权限
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        List<String> roles = userMapper.selectRolesByUserId(user.getId());

        // 合并权限
        permissions.addAll(roles);

        log.debug("用户 {} 加载成功, 权限数: {} ", username, permissions.size());

        return new SecurityUser(user, permissions);
    }
}
