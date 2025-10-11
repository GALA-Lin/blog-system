package com.blog.module.notification.service;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-21:19
 * @Description:
 */

/**
 * Notification Service Interface (Temporary)
 * TODO: Implement full notification module later
 */
public interface INotificationService {

    /**
     * Create comment notification
     * @param recipientId Recipient user ID
     * @param senderId Sender user ID
     * @param commentId Comment ID
     * @param postId Post ID
     */
    void createCommentNotification(Long recipientId, Long senderId, Long commentId, Long postId);

    /**
     * Create reply notification
     * @param recipientId Recipient user ID
     * @param senderId Sender user ID
     * @param commentId Comment ID
     * @param postId Post ID
     */
    void createReplyNotification(Long recipientId, Long senderId, Long commentId, Long postId);

    /**
     * Create like notification
     * @param recipientId Recipient user ID
     * @param senderId Sender user ID
     * @param postId Post ID
     */
    void createLikeNotification(Long recipientId, Long senderId, Long postId);

    /**
     * Create favorite notification
     * @param recipientId Recipient user ID
     * @param senderId Sender user ID
     * @param postId Post ID
     */
    void createFavoriteNotification(Long recipientId, Long senderId, Long postId);

    /**
     * Create follow notification
     * @param recipientId Recipient user ID
     * @param senderId Sender user ID
     */
    void createFollowNotification(Long recipientId, Long senderId);
}