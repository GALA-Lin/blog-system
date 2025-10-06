package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-14:35
 * @Description:
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    private String username;

    private String email;

    @JsonIgnore // Never expose password in JSON
    private String password;

    private String nickname;

    private String avatarUrl;

    private String bio;

    private Integer status; // 1=active, 0=inactive, -1=banned

    private Integer emailVerified; // 0=not verified, 1=verified

    private Integer followerCount;

    private Integer followingCount;

    private Integer postCount;

    private LocalDateTime lastLoginAt;
}