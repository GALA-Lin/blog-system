package com.blog.VO.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-14:36
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)  // 生成public无参构造函数
@AllArgsConstructor(access = AccessLevel.PUBLIC) // 生成public全参构造函数
@Schema(description = "用户个人信息VO")
public class UserProfileVO {

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

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "文章数量")
    private Integer postCount;

    @Schema(description = "粉丝数量")
    private Integer followerCount;

    @Schema(description = "关注数量")
    private Integer followingCount;

    @Schema(description = "邮箱是否验证")
    private Boolean emailVerified;

    @Schema(description = "上次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
