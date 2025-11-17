package com.blog.DTO.auth;

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
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$",
            message = "用户名只能包含字母、数字、下划线和连字符")
    @Schema(description = "Username", example = "test_user")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "非法的邮箱格式")
    @Schema(description = "Email", example = "test@example.com")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在8-32之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "密码必须包含大小写字母和数字")
    @Schema(description = "Password", example = "Test@123")
    private String password;

    @NotBlank(message = "验证密码不能为空")
    @Schema(description = "验证密码", example = "Test@123")
    private String confirmPassword;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称", example = "Test User")
    private String nickname;

    /**
     * 验证前后密码是否匹配
     */
    @AssertTrue(message = "两次输入的密码不一致")
    public boolean isPasswordMatch() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}