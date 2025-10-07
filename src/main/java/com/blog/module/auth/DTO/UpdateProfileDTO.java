package com.blog.module.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-14:39
 * @Description:
 */
@Data
@Schema(description = "Update Profile Request")
public class UpdateProfileDTO {

    @Size(max = 50, message = "Nickname length cannot exceed 50")
    @Schema(description = "Nickname")
    private String nickname;

    @Size(max = 500, message = "Bio length cannot exceed 500")
    @Schema(description = "Bio")
    private String bio;

    @Schema(description = "Avatar URL")
    private String avatarUrl;
}
