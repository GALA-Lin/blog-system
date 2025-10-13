package com.blog.DTO.favorite;

import lombok.Data;

/**
 * 收藏夹简要信息 DTO
 */
@Data
public class FolderSimpleDTO {
    private Long id;
    private String name;
    private Integer isPublic;
    private Integer postCount;
}
