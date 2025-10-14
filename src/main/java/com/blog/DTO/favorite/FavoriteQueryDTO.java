package com.blog.DTO.favorite;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 收藏查询 DTO
 */
@Data
public class FavoriteQueryDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏夹ID
     */
    private Long folderId;

    /**
     * 排序字段: created_at, post_title
     */
    private String sortBy = "created_at";

    /**
     * 排序顺序: asc, desc
     */
    private String sortOrder = "desc";

    @Min(value = 1, message = "页码必须 >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页面大小必须 >= 1")
    @Max(value = 100, message = "页面大小必须 <= 100")
    private Integer pageSize = 10;
}
