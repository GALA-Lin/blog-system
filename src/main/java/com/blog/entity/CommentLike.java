package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-10:04
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment_likes")
public class CommentLike extends BaseEntity {

    private Long commentId;

    private Long userId;

    @TableField(exist = false)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private User user;
}