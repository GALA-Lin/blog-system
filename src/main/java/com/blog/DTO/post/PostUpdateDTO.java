package com.blog.DTO.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:31
 * @Description:
 */
@Data
public class PostUpdateDTO {

    private Long id;

    @Size(max = 200, message = "文章标题最大长度为200")
    private String title;

    @Size(max = 500, message = "文章摘要最大长度为500")
    private String summary;

    private String content;

    private String coverImage;

    private List<Long> categoryIds;

    private List<Long> tagIds;

    private Integer status;
}
