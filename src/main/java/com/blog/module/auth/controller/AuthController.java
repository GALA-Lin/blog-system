package com.blog.module.auth.controller;

import com.blog.DTO.auth.ChangePasswordDTO;
import com.blog.DTO.auth.LoginDTO;
import com.blog.DTO.auth.RegisterDTO;
import com.blog.DTO.auth.UpdateProfileDTO;
import com.blog.VO.auth.LoginVO;
import com.blog.VO.auth.UserProfileVO;
import com.blog.common.Result;
import com.blog.module.auth.service.AuthService;
import com.blog.module.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-18:00
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return loginVO 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("用户尝试登录: {}", loginDTO.getUsername());
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用用户名、密码、邮箱注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        authService.register(registerDTO);
        return Result.success("注册成功");
    }
    /**
     * 用户等登出
     * @param authHeader 认证头
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录并且拉黑token")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "Get Current User Profile", description = "Get authenticated user's profile")
    public Result<UserProfileVO> getCurrentProfile() {
        UserProfileVO profile = userService.getCurrentUserProfile();
        return Result.success(profile);
    }

    /**
     * 更新用户信息
     * @param updateProfileDTO 更新信息DTO
     * @return 更新结果
     */
    @PutMapping("/profile")
    @Operation(summary = "Update Profile", description = "Update current user's profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        userService.updateProfile(updateProfileDTO);
        return Result.success("Profile updated successfully");
    }

    /**
     * 修改密码
     * @param changePasswordDTO 密码信息DTO
     * @return 修改结果
     */
    @PutMapping("/password")
    @Operation(summary = "Change Password", description = "Change current user's password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(changePasswordDTO);
        return Result.success("Password changed successfully");
    }

    /**
     * 检查用户名是否可用
     * @param username 用户名
     */
    @GetMapping("/check-username")
    @Operation(summary = "Check Username", description = "Check if username is available")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.getUserByUsername(username) != null;
        return Result.success(!exists);
    }


}