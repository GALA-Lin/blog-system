package com.blog.DTO.post;

import lombok.Data;

/**
 * 标签简要信息 DTO
 */
@Data
public class TagSimpleDTO {

    private Long id;

    private String name;

    private String slug;

    private String color;

    private Integer postCount;
}
