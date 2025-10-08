package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:19
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tags")
public class Tag extends BaseEntity {

    private String name;

    private String slug;

    private String color;

    private Integer postCount;
}

