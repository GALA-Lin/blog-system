package com.blog.module.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-02:32
 * @Description: 注册请求DTO
 */

@Data
@Schema(description = "Register Request")
public class RegisterDTO {

    /**
     * 用户名，长度3-20，只能包含字母、数字、下划线和连字符
     * 邮箱格式验证
     * 密码长度8-32，必须包含大小写字母和数字
     * 确认密码
     */
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username length must be between 3-20")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$",
            message = "Username can only contain letters, numbers, underscores and hyphens")
    @Schema(description = "Username", example = "testuser")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Schema(description = "Email", example = "test@example.com")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 32, message = "Password length must be between 8-32")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain uppercase, lowercase and digit")
    @Schema(description = "Password", example = "Test@123")
    private String password;

    @NotBlank(message = "Confirm password cannot be empty")
    @Schema(description = "Confirm Password", example = "Test@123")
    private String confirmPassword;

    @Size(max = 50, message = "Nickname length cannot exceed 50")
    @Schema(description = "Nickname", example = "Test User")
    private String nickname;

    /**
     * 验证前后密码是否匹配
     */
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatch() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}