package com.blog.module.auth.service;

import com.blog.DTO.auth.LoginDTO;
import com.blog.DTO.auth.RegisterDTO;
import com.blog.VO.auth.LoginVO;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-16:54
 * @Description: 认证服务接口
 */
public interface AuthService {
    /**
     * 用户登录
     * @param loginDTO 登录信息
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册信息
     */
    void register(RegisterDTO registerDTO);
    /**
     * 用户退出
     * @param token 登录token
     */
    void logout(String token);
}
