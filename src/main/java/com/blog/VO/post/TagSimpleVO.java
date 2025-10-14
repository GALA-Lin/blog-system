package com.blog.VO.post;

import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-11:13
 * @Description:
 */
@Data
public class TagSimpleVO {

    private Long id;

    private String name;

    private String slug;

    private String color;

    private Integer postCount;
}