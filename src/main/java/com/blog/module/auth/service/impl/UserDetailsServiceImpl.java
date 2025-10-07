package com.blog.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.User;
import com.blog.module.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-15:23
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl extends UserDetailsService {
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
            log.error("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
