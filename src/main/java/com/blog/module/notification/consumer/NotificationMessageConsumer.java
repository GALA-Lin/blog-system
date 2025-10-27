package com.blog.module.notification.consumer;

import com.blog.DTO.mq.NotificationMessage;
import com.blog.config.RabbitMQConfig;
import com.blog.entity.Notification;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.module.notification.mapper.NotificationMapper;
import com.blog.module.notification.service.impl.NotificationServiceImpl;
import com.blog.module.post.mapper.PostMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.blog.constants.SystemConstants.*;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-24-10:02
 * @Description:
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleNotificationMessage(NotificationMessage message, Message mqMessage, Channel channel) {
        try {
            log.info("【MQ消费】收到通知消息: {}", message);

            // 防重复（1小时内相同通知不重复发送）
            if (Boolean.TRUE.equals(notificationMapper.existsSimilarNotification(
                    message.getRecipientId(),
                    message.getSenderId(),
                    message.getType(),
                    message.getRelatedId()))) {
                log.info("【MQ消费】跳过重复通知: {}", message);
                channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            // 根据类型处理
            switch (message.getType()) {
                case "COMMENT" -> handleCommentNotification(message);
                case "REPLY" -> handleReplyNotification(message);
                case "LIKE" -> handleLikeNotification(message);
                case "LIKE_COMMENT" -> handleCommentLikeNotification(message);
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

    private void handleCommentLikeNotification(NotificationMessage message) {
        User sender = userMapper.selectById(message.getSenderId());
        if (sender == null) {
            log.warn("【通知处理】发送者不存在: {}", message.getSenderId());
            return;
        }
        Long postId = extractPostId(message.getContent());
        Post post = postId != null ? postMapper.selectById(postId) : null;
        if (post == null) {
            log.warn("【通知处理】文章不存在: postId={}", postId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(message.getRecipientId());
        notification.setSenderId(message.getSenderId());
        notification.setType("LIKE_COMMENT");
        notification.setTitle("评论点赞通知");
        notification.setContent(String.format("%s 赞了您的评论",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername()));
        notification.setLinkUrl(""); // 可根据需要设置链接
        notification.setRelatedId(message.getRelatedId());
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        incrementUnreadCount(message.getRecipientId(), "LIKE_COMMENT");

        log.info("【数据库保存】评论点赞通知创建成功: recipientId={}, commentId={}",
                message.getRecipientId(), message.getRelatedId());
    }

    private void handleLikeNotification(NotificationMessage message) {
        User sender = userMapper.selectById(message.getSenderId());
        if (sender == null) {
            return;
        }

        Post post = postMapper.selectById(message.getRelatedId());
        if (post == null) {
            log.warn("【通知处理】文章不存在: postId={}", message.getRelatedId());
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(message.getRecipientId());
        notification.setSenderId(message.getSenderId());
        notification.setType("LIKE");
        notification.setTitle("新点赞通知");
        notification.setContent(String.format("%s 赞了您的文章《%s》",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername(),
                post.getTitle()));
        notification.setLinkUrl("/posts/" + message.getRelatedId());
        notification.setRelatedId(message.getRelatedId());
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        incrementUnreadCount(message.getRecipientId(), "LIKE");

        log.info("【数据库保存】点赞通知创建成功: recipientId={}, postId={}",
                message.getRecipientId(), message.getRelatedId());
    }

    /**
     * 处理评论通知
     */
    private void handleCommentNotification(NotificationMessage message) {
        // ✅ 修复：只查询 sender，用于生成通知内容
        User sender = userMapper.selectById(message.getSenderId());
        if (sender == null) {
            log.warn("【通知处理】发送者不存在: {}", message.getSenderId());
            return;
        }

        Long postId = extractPostId(message.getContent());
        Post post = postId != null ? postMapper.selectById(postId) : null;
        if (post == null) {
            log.warn("【通知处理】文章不存在: postId={}", postId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(message.getRecipientId());
        notification.setSenderId(message.getSenderId());
        notification.setType("COMMENT");
        notification.setTitle("新评论通知");
        notification.setContent(String.format("%s 评论了您的文章《%s》",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername(),
                post.getTitle()));
        notification.setLinkUrl("/posts/" + postId + "#comment-" + message.getRelatedId());
        notification.setRelatedId(message.getRelatedId());
        notification.setIsRead(0);


        notificationMapper.insert(notification);
        // Redis 未读计数
        incrementUnreadCount(message.getRecipientId(), "COMMENT");

        log.info("【数据库保存】评论通知创建成功: recipientId={}, commentId={}",
                message.getRecipientId(), message.getRelatedId());
    }

    /**
     * 增加未读计数（Redis）
     */
    private void incrementUnreadCount(Long userId, String type) {
        String totalKey = String.format(KEY_USER_UNREAD_COUNT, userId);
        String typeKey = String.format(KEY_USER_UNREAD_BY_TYPE, userId, type);

        redisTemplate.opsForValue().increment(totalKey, 1);
        redisTemplate.expire(totalKey, 1, TimeUnit.HOURS);

        redisTemplate.opsForValue().increment(typeKey, 1);
        redisTemplate.expire(typeKey, 1, TimeUnit.HOURS);

        log.debug("【Redis更新】未读计数增加: userId={}, type={}", userId, type);
    }

    /**
     * 从 content 中提取 postId
     */
    private Long extractPostId(String content) {
        if (content != null && content.startsWith("postId:")) {
            try {
                return Long.parseLong(content.substring(7));
            } catch (NumberFormatException e) {
                log.warn("【解析失败】无法从content提取postId: {}", content);
            }
        }
        return null;
    }

    /**
     * 处理回复通知
     */
    private void handleReplyNotification(NotificationMessage message) {
        User sender = userMapper.selectById(message.getSenderId());
        if (sender == null) {
            return;
        }

        Long postId = extractPostId(message.getContent());
        Post post = postId != null ? postMapper.selectById(postId) : null;

        Notification notification = new Notification();
        notification.setUserId(message.getRecipientId());
        notification.setSenderId(message.getSenderId());
        notification.setType("REPLY");
        notification.setTitle("新回复通知");
        notification.setContent(String.format("%s 回复了您的评论",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername()));
        notification.setLinkUrl(post != null ?
                "/posts/" + postId + "#comment-" + message.getRelatedId() : "");
        notification.setRelatedId(message.getRelatedId());
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        incrementUnreadCount(message.getRecipientId(), "REPLY");

        log.info("【数据库保存】回复通知创建成功: recipientId={}, commentId={}",
                message.getRecipientId(), message.getRelatedId());
    }

    /**
     * 处理收藏通知
     */
    private void handleFavoriteNotification(NotificationMessage message) {
        User sender = userMapper.selectById(message.getSenderId());
        Post post = postMapper.selectById(message.getRelatedId());

        if (sender == null || post == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(message.getRecipientId());
        notification.setSenderId(message.getSenderId());
        notification.setType("FAVORITE");
        notification.setTitle("新收藏通知");
        notification.setContent(String.format("%s 收藏了您的文章《%s》",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername(),
                post.getTitle()));
        notification.setLinkUrl("/posts/" + message.getRelatedId());
        notification.setRelatedId(message.getRelatedId());
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        incrementUnreadCount(message.getRecipientId(), "FAVORITE");

        log.info("【数据库保存】收藏通知创建成功: recipientId={}, postId={}",
                message.getRecipientId(), message.getRelatedId());
    }

    /**
     * 处理关注通知
     */
    private void handleFollowNotification(NotificationMessage message) {
        User sender = userMapper.selectById(message.getSenderId());
        if (sender == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(message.getRecipientId());
        notification.setSenderId(message.getSenderId());
        notification.setType("FOLLOW");
        notification.setTitle("新关注通知");
        notification.setContent(String.format("%s 关注了您",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername()));
        notification.setLinkUrl("/users/" + message.getSenderId());
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        incrementUnreadCount(message.getRecipientId(), "FOLLOW");

        log.info("【数据库保存】关注通知创建成功: recipientId={}, senderId={}",
                message.getRecipientId(), message.getSenderId());
    }

}
