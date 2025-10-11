package com.blog.DTO.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-16:22
 * @Description:
 */
@Data
public class CommentUpdateDTO {

    @NotNull(message = "评论ID不能为空")
    private Long id;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 5000, message = "评论内容不能超过5000个字符")
    private String content;

}
