package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-09:39
 * @Description: 评论实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comments")
public class Comment extends BaseEntity {

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID（顶级评论为null）
     */
    private Long parentId;

    /**
     * 根评论ID（评论树的根节点）
     */
    private Long rootId;

    /**
     * 回复目标用户ID
     */
    private Long replyToUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 状态: 1=已审核, 0=待审核, -1=已删除, -2=垃圾评论
     */
    private Integer status;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    // ========== 以下是瞬态字段（不存储在数据库中）==========

    /**
     * 评论作者信息（关联查询）
     */
    @TableField(exist = false)
    private User author;

    /**
     * 回复目标用户信息（关联查询）
     */
    @TableField(exist = false)
    private User replyToUser;

    /**
     * 子评论列表
     */
    @TableField(exist = false)
    private java.util.List<Comment> children;

    /**
     * 当前用户是否已点赞
     */
    @TableField(exist = false)
    private Boolean isLiked;

    /**
     * 当前用户是否是作者
     */
    @TableField(exist = false)
    private Boolean isAuthor;
}