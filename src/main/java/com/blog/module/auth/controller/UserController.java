package com.blog.module.auth.controller;

import com.blog.common.Result;
import com.blog.module.auth.service.UserService;
import com.blog.VO.auth.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "User profile and management APIs")
public class UserController {

    private final UserService userService;

    /**
     * Get User Profile by ID
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get User Profile", description = "Get user profile by user ID")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        UserProfileVO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    /**
     * Admin: Get User Details (with more info)
     */
    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "[Admin] Get User Details", description = "Get detailed user information (Admin only)")
    public Result<UserProfileVO> getUserDetails(@PathVariable Long userId) {
        UserProfileVO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }
}