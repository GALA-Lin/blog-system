package com.blog.module.auth.service;

import com.blog.entity.User;
import com.blog.DTO.auth.ChangePasswordDTO;
import com.blog.DTO.auth.UpdateProfileDTO;
import com.blog.VO.auth.UserProfileVO;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-17:46
 * @Description:
 */
public interface UserService {
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserProfileVO getUserProfile(Long userId);
    /**
     * 获取当前用户信息
     * @return 当前用户信息
     */
    UserProfileVO getCurrentUserProfile();
    /**
     * 更新用户信息
     * @param updateProfileDTO 更新用户信息DTO
     */
    void updateProfile(UpdateProfileDTO updateProfileDTO);
    /**
     * 修改密码
     * @param changePasswordDTO 修改密码DTO
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

}
