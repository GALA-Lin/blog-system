package com.blog.DTO.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:21
 * @Description:
 */
@Data
public class FolderUpdateDTO {

    @NotNull(message = "收藏夹ID不能为空")
    private Long id;

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 50, message = "收藏夹名称不能超过50个字符")
    private String name;

    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;

    private Integer isPublic;

    private Integer sortOrder;
}
