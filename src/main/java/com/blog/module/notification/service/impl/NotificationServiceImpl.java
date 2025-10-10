package com.blog.module.notification.service.impl;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-21:20
 * @Description:
 */
import com.blog.module.notification.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notification Service Implementation (Temporary)
 * TODO: Implement full notification logic later
 */
@Slf4j
@Service
public class NotificationServiceImpl implements INotificationService {

    @Override
    @Async
    public void createCommentNotification(Long recipientId, Long senderId, Long commentId, Long postId) {
        log.info("TODO: Send comment notification - recipient: {}, sender: {}, comment: {}, post: {}",
                recipientId, senderId, commentId, postId);
        // TODO: Implement notification creation
        // 1. Insert notification record to database
        // 2. Send WebSocket message if user is online
        // 3. Send email if user enabled email notification
    }

    @Override
    @Async
    public void createReplyNotification(Long recipientId, Long senderId, Long commentId, Long postId) {
        log.info("TODO: Send reply notification - recipient: {}, sender: {}, comment: {}, post: {}",
                recipientId, senderId, commentId, postId);
        // TODO: Implement notification creation
    }

    @Override
    @Async
    public void createLikeNotification(Long recipientId, Long senderId, Long postId) {
        log.info("TODO: Send like notification - recipient: {}, sender: {}, post: {}",
                recipientId, senderId, postId);
        // TODO: Implement notification creation
    }

    @Override
    @Async
    public void createFavoriteNotification(Long recipientId, Long senderId, Long postId) {
        log.info("TODO: Send favorite notification - recipient: {}, sender: {}, post: {}",
                recipientId, senderId, postId);
        // TODO: Implement notification creation
    }

    @Override
    @Async
    public void createFollowNotification(Long recipientId, Long senderId) {
        log.info("TODO: Send follow notification - recipient: {}, sender: {}",
                recipientId, senderId);
        // TODO: Implement notification creation
    }
}
