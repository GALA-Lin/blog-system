package com.blog.DTO.post;

import lombok.Data;

/**
 * 分类简要信息 DTO
 */
@Data
public class CategorySimpleDTO {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private String icon;

    private Integer postCount;
}
