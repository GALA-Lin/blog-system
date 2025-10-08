package com.blog.module.auth.controller;

import com.blog.common.Result;
import com.blog.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller - For testing authentication and authorization
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "Test APIs for development")
public class TestController {

    /**
     * Public endpoint - no authentication required
     */
    @GetMapping("/public")
    @Operation(summary = "Public Endpoint", description = "No authentication required")
    public Result<String> publicEndpoint() {
        return Result.success();
    }

    /**
     * Protected endpoint - authentication required
     */
    @GetMapping("/protected")
    @Operation(summary = "Protected Endpoint", description = "Authentication required")
    public Result<Map<String, Object>> protectedEndpoint() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "This is a protected endpoint");
        data.put("userId", SecurityUtil.getCurrentUserId());
        data.put("username", SecurityUtil.getCurrentUsername());
        return Result.success(data);
    }

    /**
     * Admin only endpoint
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Admin Endpoint", description = "Admin role required")
    public Result<String> adminEndpoint() {
        return Result.success();
    }

    /**
     * Author or Editor endpoint
     */
    @GetMapping("/author")
    @PreAuthorize("hasAnyRole('ROLE_AUTHOR', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Author Endpoint", description = "Author/Editor/Admin role required")
    public Result<String> authorEndpoint() {
        return Result.success();
    }

    /**
     * Permission-based endpoint
     */
    @GetMapping("/create-post")
    @PreAuthorize("hasAuthority('post:create')")
    @Operation(summary = "Create Post Permission", description = "post:create permission required")
    public Result<String> createPostEndpoint() {
        return Result.success();
    }

    /**
     * Check current user info
     */
    @GetMapping("/whoami")
    @Operation(summary = "Who Am I", description = "Get current authenticated user info")
    public Result<Map<String, Object>> whoami() {
        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", SecurityUtil.isAuthenticated());
        data.put("userId", SecurityUtil.getCurrentUserId());
        data.put("username", SecurityUtil.getCurrentUsername());
        return Result.success(data);
    }
}