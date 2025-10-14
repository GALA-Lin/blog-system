package com.blog.DTO.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:18
 * @Description:
 */
@Data
public class FolderCreateDTO {

    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 50, message = "文件夹名称不能超过50个字符")
    private String name;

    @Size(max = 200, message = "文件夹描述不能超过200个字符")
    private String description;

    @NotNull(message = "请设置收藏夹公开状态")
    private Integer isPublic; // 0:私密 1:公开

    private Integer sortOrder;

}
