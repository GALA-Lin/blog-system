package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-12-16:26
 * @Description:
 */
@Data
@TableName("favorites")
public class Favorite extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 收藏夹ID（null表示默认收藏夹）
     */
    private Long folderId;

    /**
     * 个人笔记
     */
    private String notes;

    @TableField(exist = false)
    private LocalDateTime updatedAt;

    // ========== 瞬态字段 ==========

    /**
     * 文章信息（关联查询）
     */
    @TableField(exist = false)
    private Post post;

    /**
     * 用户信息（关联查询）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 收藏夹信息（关联查询）
     */
    @TableField(exist = false)
    private FavoriteFolder folder;
}
