package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:25
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("categories")
public class Category extends BaseEntity {

    private Long parentId;

    private String name;

    private String slug;

    private String description;

    private String icon;

    private Integer sortOrder;

    private Integer postCount;

    private Integer status;
}
