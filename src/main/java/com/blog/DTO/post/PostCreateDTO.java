package com.blog.DTO.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:28
 * @Description:
 */
@Data
public class PostCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private List<Long> categoryIds;

    private List<Long> tagIds;

    @NotNull(message = "状态不能为空")
    private Integer status; // 0: 草稿 1: 发布
}
