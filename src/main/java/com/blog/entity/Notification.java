package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-17-21:53
 * @Description:
 */
@Data
@TableName("notifications")
public class Notification extends BaseEntity {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 接收通知的用户ID
     */
    private Long userId;

    /**
     * 发送通知的用户ID（null表示系统通知）
     */
    private Long senderId;

    /**
     * 通知类型：COMMENT, REPLY, LIKE, FAVORITE, FOLLOW, SYSTEM
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 相关链接URL
     */
    private String linkUrl;

    /**
     * 相关实体ID（文章ID、评论ID等）
     */
    private Long relatedId;

    /**
     * 是否已读：0=未读, 1=已读
     */
    private Integer isRead;

    @TableField(exist = false)
    private LocalDateTime updatedAt;

    // ========== 瞬态字段 ==========

    /**
     * 发送者信息（关联查询）
     */
    @TableField(exist = false)
    private User sender;
}
