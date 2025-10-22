package com.blog.VO.notification;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-17-22:16
 * @Description: 通知统计VO
 */

import lombok.Data;

@Data
public class NotificationStatVO {

    /**
     * 未读通知总数
     */
    private Long unreadCount;

    /**
     * 未读评论通知数
     */
    private Long unreadCommentCount;

    /**
     * 未读回复通知数
     */
    private Long unreadReplyCount;

    /**
     * 未读点赞通知数
     */
    private Long unreadLikeCount;

    /**
     * 未读收藏通知数
     */
    private Long unreadFavoriteCount;

    /**
     * 未读关注通知数
     */
    private Long unreadFollowCount;

    /**
     * 未读系统通知数
     */
    private Long unreadSystemCount;
}