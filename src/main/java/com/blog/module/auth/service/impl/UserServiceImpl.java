package com.blog.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.BusinessException;
import com.blog.common.ResultCode;
import com.blog.entity.User;
import com.blog.DTO.auth.ChangePasswordDTO;
import com.blog.DTO.auth.UpdateProfileDTO;
import com.blog.VO.auth.UserProfileVO;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.module.auth.service.UserService;
import com.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-17:50
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "user:profile", key = "#userId")
    public UserProfileVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserProfileVO profileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, profileVO);
        profileVO.setEmailVerified(user.getEmailVerified() == 1);

        return profileVO;
    }

    @Override
    public UserProfileVO getCurrentUserProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return getUserProfile(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user:profile", key = "#result")
    public void updateProfile(UpdateProfileDTO updateProfileDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // Update fields
        if (updateProfileDTO.getNickname() != null) {
            user.setNickname(updateProfileDTO.getNickname());
        }
        if (updateProfileDTO.getBio() != null) {
            user.setBio(updateProfileDTO.getBio());
        }
        if (updateProfileDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateProfileDTO.getAvatarUrl());
        }

        userMapper.updateById(user);
        log.info("User profile updated: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // Verify old password
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH, "Old password is incorrect");
        }

        // Check if new password matches confirm password
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH, "Passwords do not match");
        }

        // Encrypt new password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userMapper.updateById(user);

        log.info("Password changed for user: {}", userId);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    @Cacheable(value = "user", key = "#userId")
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}