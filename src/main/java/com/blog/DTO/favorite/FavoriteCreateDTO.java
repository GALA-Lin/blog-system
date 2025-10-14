package com.blog.DTO.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:25
 * @Description:
 */
@Data
public class FavoriteCreateDTO {

    @NotNull(message = "收藏的文章ID不能为空")
    private Long postId;

    private Long folderId; // null表示收藏到默认收藏夹列表

    @Size(max = 500, message = "笔记不能超过500个字符")
    private String notes;

}
