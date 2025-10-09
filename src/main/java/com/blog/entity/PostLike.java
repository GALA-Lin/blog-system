package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-10:03
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_likes")
public class PostLike extends BaseEntity {

    private Long postId;

    private Long userId;

    @TableField(exist = false)
    private LocalDateTime updatedAt;
}
