package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-17:25
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "post_tags")
public class PostTag extends BaseEntity {
    private Long postId;
    private Long tagId;
}
