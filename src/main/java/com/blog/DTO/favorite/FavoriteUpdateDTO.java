package com.blog.DTO.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.checkerframework.checker.units.qual.N;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:27
 * @Description:
 */
@Data
public class FavoriteUpdateDTO {

    private Long id;

    @NotBlank(message = "收藏夹ID不能为空")
    private Long favoriteId;

    private Long userId;

    private Long postId;


    private Long folderId;

    @Size(max = 500, message = "笔记不能超过500个字符")
    private String notes;

}
