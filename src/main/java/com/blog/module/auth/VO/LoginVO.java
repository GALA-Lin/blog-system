package com.blog.module.auth.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-13:56
 * @Description: 登陆响应
 */
@Data
@Builder
@Schema(description = "登陆响应VO")
public class LoginVO {

    @Schema(description = "Access Token")
    private String token;

    @Schema(description = "Token Type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Token 过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Builder
    @Schema(description = "用户信息")
    public static class UserInfo {

        @Schema(description = "User ID")
        private Long id;

        @Schema(description = "Username")
        private String username;

        @Schema(description = "Email")
        private String email;

        @Schema(description = "Nickname")
        private String nickname;

        @Schema(description = "Avatar URL")
        private String avatarUrl;

        @Schema(description = "User Roles")
        private List<String> roles;

        @Schema(description = "User Permissions")
        private List<String> permissions;
    }
}
