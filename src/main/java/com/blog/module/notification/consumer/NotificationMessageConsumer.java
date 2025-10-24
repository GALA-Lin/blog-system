package com.blog.module.notification.consumer;

import com.blog.DTO.mq.NotificationMessage;
import com.blog.config.RabbitMQConfig;
import com.blog.module.notification.service.impl.NotificationServiceImpl;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-24-10:02
 * @Description:
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationServiceImpl notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationMessage(NotificationMessage message, Message mqMessage, Channel channel) {
        try {
            log.info("【MQ消费】收到通知消息: {}", message);

            // 根据通知类型处理
            switch (message.getType()) {
                case "LIKE" -> handleLikeNotification(message);
                case "LIKE_COMMENT" -> handleCommentLikeNotification(message);
                case "COMMENT" -> handleCommentNotification(message);
                case "REPLY" -> handleReplyNotification(message);
                case "FAVORITE" -> handleFavoriteNotification(message);
                case "FOLLOW" -> handleFollowNotification(message);
                default -> log.warn("【MQ消费】未知通知类型: {}", message.getType());
            }

            // 手动确认
            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
            log.info("【MQ消费】通知消息处理成功: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("【MQ消费】处理通知消息失败: {}", message, e);
            try {
                // 重新入队
                channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception ex) {
                log.error("【MQ消费】消息重新入队失败", ex);
            }
        }
    }

    /**
     * 处理点赞通知
     */
    private void handleLikeNotification(NotificationMessage message) {
        log.info("【通知处理】文章点赞通知: recipientId={}, senderId={}, postId={}",
                message.getRecipientId(), message.getSenderId(), message.getRelatedId());

        // TODO: 实现通知逻辑
        // 1. 保存通知记录到数据库
        // 2. 如果用户在线，推送WebSocket消息
        // 3. 如果用户开启了邮件通知，发送邮件  
    }

    /**
     * 处理评论点赞通知
     */
    private void handleCommentLikeNotification(NotificationMessage message) {
        log.info("【通知处理】评论点赞通知: recipientId={}, senderId={}, commentId={}",
                message.getRecipientId(), message.getSenderId(), message.getRelatedId());
    }

    /**
     * 处理评论通知
     */
    private void handleCommentNotification(NotificationMessage message) {
        log.info("【通知处理】评论通知: recipientId={}, senderId={}, commentId={}",
                message.getRecipientId(), message.getSenderId(), message.getRelatedId());
    }

    /**
     * 处理回复通知
     */
    private void handleReplyNotification(NotificationMessage message) {
        log.info("【通知处理】回复通知: recipientId={}, senderId={}, commentId={}",
                message.getRecipientId(), message.getSenderId(), message.getRelatedId());
    }

    /**
     * 处理收藏通知
     */
    private void handleFavoriteNotification(NotificationMessage message) {
        log.info("【通知处理】收藏通知: recipientId={}, senderId={}, postId={}",
                message.getRecipientId(), message.getSenderId(), message.getRelatedId());
    }

    /**
     * 处理关注通知
     */
    private void handleFollowNotification(NotificationMessage message) {
        log.info("【通知处理】关注通知: recipientId={}, senderId={}",
                message.getRecipientId(), message.getSenderId());
    }

}
