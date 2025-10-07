package com.blog.module.auth.service;

import com.blog.module.auth.DTO.LoginDTO;
import com.blog.module.auth.DTO.RegisterDTO;
import com.blog.module.auth.VO.LoginVO;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-16:54
 * @Description: 认证服务接口
 */
public interface AuthService {
    /**
     * 用户登陆
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);
    /**
     * 用户退出
     */
    void logout(String token);
}
