package com.blog.module.notification.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.DTO.mq.NotificationMessage;
import com.blog.DTO.notification.NotificationQueryDTO;
import com.blog.DTO.notification.SystemNotificationDTO;
import com.blog.VO.auth.UserSimpleVO;
import com.blog.VO.notification.NotificationStatVO;
import com.blog.VO.notification.NotificationVO;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.config.RabbitMQConfig;
import com.blog.constants.SystemConstants;
import com.blog.entity.Notification;
import com.blog.entity.User;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.module.notification.mapper.NotificationMapper;
import com.blog.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-21:20
 * @Description:
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    // ========== 创建通知（通过 MQ 异步处理）==========

    @Override
    public void sendCommentNotification(Long recipientId, Long senderId, Long commentId, Long postId) {
        if (recipientId.equals(senderId)) {
            return; // 不给自己发通知
        }

        // 构建通知消息
        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .recipientId(recipientId)
                .senderId(senderId)
                .type("COMMENT")
                .relatedId(commentId)
                .timestamp(LocalDateTime.now())
                .build();

        // 添加额外内容（postId）
        message.setContent("postId:" + postId);

        log.info("【MQ发送】评论通知消息: {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.comment",
                message
        );
    }

    @Override
    public void sendReplyNotification(Long recipientId, Long senderId, Long commentId, Long postId) {

    }

    @Override
    public void sendLikeNotification(Long recipientId, Long senderId, Long relatedId, String type) {

    }

    @Override
    public void sendFavoriteNotification(Long recipientId, Long senderId, Long postId) {

    }

    @Override
    public void sendFollowNotification(Long recipientId, Long senderId) {

    }

    @Override
    public void createSystemNotification(SystemNotificationDTO dto) {

    }

    @Override
    public PageResult<NotificationVO> getUserNotifications(NotificationQueryDTO queryDTO, Long userId) {
        return null;
    }

    @Override
    public NotificationVO getNotificationDetail(Long notificationId, Long userId) {
        return null;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return 0L;
    }

    @Override
    public NotificationStatVO getNotificationStats(Long userId) {
        return null;
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {

    }

    @Override
    public void batchMarkAsRead(List<Long> ids, Long userId) {

    }

    @Override
    public void markAllAsRead(Long userId) {

    }

    @Override
    public void markTypeAsRead(String type, Long userId) {

    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {

    }

    @Override
    public void batchDeleteNotifications(List<Long> ids, Long userId) {

    }

    @Override
    public void clearReadNotifications(Long userId) {

    }

    @Override
    public void cleanOldNotifications(Long userId, Integer days) {

    }
}
