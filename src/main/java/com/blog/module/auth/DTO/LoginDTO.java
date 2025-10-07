package com.blog.module.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-02:24
 * @Description: 登陆DTO
 */
@Data
@Schema(description = "Login Request")
public class LoginDTO {
    /**
     * 用户名
     * 密码
     */
    @NotBlank(message = "Username cannot be empty")
    @Schema(description = "Username", example = "admin")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Schema(description = "Password", example = "Admin@123")
    private String password;
}
