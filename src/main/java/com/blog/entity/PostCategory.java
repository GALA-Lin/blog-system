package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-17:21
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_categories")
public class PostCategory extends BaseEntity {

    private Long postId;

    private Long categoryId;
}
