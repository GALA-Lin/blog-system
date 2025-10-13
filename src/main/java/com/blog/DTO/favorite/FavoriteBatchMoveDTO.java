package com.blog.DTO.favorite;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量移动收藏 DTO
 */
@Data
public class FavoriteBatchMoveDTO {

    @NotEmpty(message = "收藏ID列表不能为空")
    private List<Long> favoriteIds;

    @NotNull(message = "目标收藏夹ID不能为空")
    private Long targetFolderId;
}
