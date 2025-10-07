package com.blog.module.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-13:54
 * @Description:
 */
@Data
@Schema(description = "修改密码DTO")
public class ChangePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "Old Password")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在8到32位之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "密码至少包含大写字母，小写字母和数字")
    @Schema(description = "新密码")
    private String newPassword;

    @NotBlank(message = "确认新密码不能为空")
    @Schema(description = "验证新密码")
    private String confirmPassword;
}

