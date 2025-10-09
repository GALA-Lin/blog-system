package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-09:39
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comments")
public class Comment extends BaseEntity {

    private Long postId;

    private Long userId;

    private Long parentId;

    private Long rootId;

    private Long replyToUserId;

    private String content;

    private Integer status; // 1: 已审核 0: 待审核 -1: 已删除 -2: 垃圾评论

    private Integer likeCount;

    private Integer replyCount;

    private String ipAddress;

    private String userAgent;
}
