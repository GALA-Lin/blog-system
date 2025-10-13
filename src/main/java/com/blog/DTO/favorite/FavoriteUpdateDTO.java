package com.blog.DTO.favorite;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.checkerframework.checker.units.qual.N;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:27
 * @Description:
 */
@Data
public class FavoriteUpdateDTO {

    @NotBlank(message = "收藏夹ID不能为空")
    private Long favoriteId;

}
