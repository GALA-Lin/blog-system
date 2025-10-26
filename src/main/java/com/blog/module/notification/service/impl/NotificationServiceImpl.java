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
import com.blog.util.SecurityUtil;
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

    // Redis Key 前缀
    private static final String KEY_USER_UNREAD_COUNT = "notification:user:%d:unread";
    private static final String KEY_USER_UNREAD_BY_TYPE = "notification:user:%d:type:%s";

    // ========== 创建通知（通过 MQ 异步处理）==========

    @Override
    public void sendCommentNotification(Long recipientId, Long senderId, Long commentId, Long postId) {
        if (recipientId.equals(senderId)) {
            return; // 不给自己发通知
        }

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
        if (recipientId.equals(senderId)) {
            return;
        }

        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .recipientId(recipientId)
                .senderId(senderId)
                .type("REPLY")
                .relatedId(commentId)
                .content("postId:" + postId)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("【MQ发送】回复通知消息: {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.reply",
                message
        );
    }

    @Override
    public void sendLikeNotification(Long recipientId, Long senderId, Long relatedId, String type) {
        if (recipientId.equals(senderId)) {
            return;
        }

        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .recipientId(recipientId)
                .senderId(senderId)
                .type(type) // LIKE 或 LIKE_COMMENT
                .relatedId(relatedId)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("【MQ发送】点赞通知消息: {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.like",
                message
        );
    }

    @Override
    public void sendFavoriteNotification(Long recipientId, Long senderId, Long postId) {
        if (recipientId.equals(senderId)) {
            return;
        }

        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .recipientId(recipientId)
                .senderId(senderId)
                .type("FAVORITE")
                .relatedId(postId)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("【MQ发送】收藏通知消息: {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.favorite",
                message
        );
    }

    @Override
    public void sendFollowNotification(Long recipientId, Long senderId) {
        if (recipientId.equals(senderId)) {
            return;
        }

        NotificationMessage message = NotificationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .recipientId(recipientId)
                .senderId(senderId)
                .type("FOLLOW")
                .timestamp(LocalDateTime.now())
                .build();

        log.info("【MQ发送】关注通知消息: {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.follow",
                message
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSystemNotification(SystemNotificationDTO dto) {

        Long senderId = SecurityUtil.getCurrentUserId();
        if (senderId == null) {
            throw new BusinessException("系统通知必须指定发送者");
        }

        List<Long> targetUsers = dto.getUserIds();
        if (targetUsers == null || targetUsers.isEmpty()) {
            // 全体用户
            targetUsers = userMapper.selectList(null).stream()
                    .map(User::getId)
                    .toList();
        }

        for (Long userId : targetUsers) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setSenderId(senderId);
            notification.setType("SYSTEM");
            notification.setTitle(dto.getTitle());
            notification.setContent(dto.getContent());
            notification.setLinkUrl(dto.getLinkUrl());
            notification.setIsRead(0);
            notification.setSender(userMapper.selectById(senderId));

            notificationMapper.insert(notification);

            // 更新 Redis 未读计数
            incrementUnreadCount(userId, "SYSTEM");
        }

        log.info("【系统通知】创建成功: senderId={}, targetCount={}", senderId, targetUsers.size());
    }

    // ========== 查询通知 ==========

    @Override
    public PageResult<NotificationVO> getUserNotifications(NotificationQueryDTO queryDTO, Long userId) {
        Page<Notification> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Notification> notificationPage = notificationMapper.selectUserNotificationsWithSender(
                page,
                userId,
                queryDTO.getType(),
                queryDTO.getIsRead(),
                queryDTO.getSortBy(),
                queryDTO.getSortOrder()
        );

        List<NotificationVO> voList = notificationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, notificationPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationVO getNotificationDetail(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);

        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该通知");
        }

        // 自动标记为已读
        if (notification.getIsRead() == 0) {
            markAsRead(notificationId, userId);
        }

        return convertToVO(notification);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        String key = String.format(KEY_USER_UNREAD_COUNT, userId);

        // 优先从 Redis 获取
        Object count = redisTemplate.opsForValue().get(key);
        if (count != null) {
            log.debug("【Redis查询】未读通知数: userId={}, count={}", userId, count);
            return ((Number) count).longValue();
        }

        // Redis 未命中，查数据库
        Long dbCount = notificationMapper.countUnread(userId);
        log.debug("【数据库查询】未读通知数: userId={}, count={}", userId, dbCount);

        // 回写 Redis
        redisTemplate.opsForValue().set(key, dbCount, 1, TimeUnit.HOURS);

        return dbCount;
    }

    @Override
    public NotificationStatVO getNotificationStats(Long userId) {
        NotificationStatVO stats = new NotificationStatVO();

        // 总未读数
        stats.setUnreadCount(getUnreadCount(userId));

        // 按类型统计
        String[] types = {"COMMENT", "REPLY", "LIKE", "FAVORITE", "FOLLOW", "SYSTEM"};
        for (String type : types) {
            Long count = getUnreadCountByType(userId, type);
            switch (type) {
                case "COMMENT" -> stats.setUnreadCommentCount(count);
                case "REPLY" -> stats.setUnreadReplyCount(count);
                case "LIKE" -> stats.setUnreadLikeCount(count);
                case "FAVORITE" -> stats.setUnreadFavoriteCount(count);
                case "FOLLOW" -> stats.setUnreadFollowCount(count);
                case "SYSTEM" -> stats.setUnreadSystemCount(count);
            }
        }

        return stats;
    }

    /**
     * 获取指定类型的未读数量
     */
    private Long getUnreadCountByType(Long userId, String type) {
        String key = String.format(KEY_USER_UNREAD_BY_TYPE, userId, type);

        Object count = redisTemplate.opsForValue().get(key);
        if (count != null) {
            return ((Number) count).longValue();
        }

        // 查数据库（需要在 Mapper 中添加方法）
        Long dbCount = notificationMapper.countUnreadByType(userId, type);
        redisTemplate.opsForValue().set(key, dbCount, 1, TimeUnit.HOURS);

        return dbCount;
    }

    // ========== 标记已读 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);

        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此通知");
        }

        if (notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);

            // 更新 Redis 计数
            decrementUnreadCount(userId, notification.getType());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 查询这些通知的类型（用于更新 Redis）
        List<Notification> notifications = notificationMapper.selectBatchIds(ids);
        Map<String, Long> typeCounts = notifications.stream()
                .filter(n -> n.getIsRead() == 0)
                .collect(Collectors.groupingBy(Notification::getType, Collectors.counting()));

        // 批量更新数据库
        int updated = notificationMapper.batchMarkAsRead(userId, ids);
        log.info("【批量标记已读】userId={}, count={}", userId, updated);

        // 更新 Redis
        typeCounts.forEach((type, count) -> {
            String typeKey = String.format(KEY_USER_UNREAD_BY_TYPE, userId, type);
            redisTemplate.opsForValue().decrement(typeKey, count);
        });

        String totalKey = String.format(KEY_USER_UNREAD_COUNT, userId);
        redisTemplate.opsForValue().decrement(totalKey, updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        int updated = notificationMapper.markAllRead(userId);
        log.info("【标记全部已读】userId={}, count={}", userId, updated);

        // 清空 Redis 计数
        String totalKey = String.format(KEY_USER_UNREAD_COUNT, userId);
        redisTemplate.delete(totalKey);

        String[] types = {"COMMENT", "REPLY", "LIKE", "FAVORITE", "FOLLOW", "SYSTEM"};
        for (String type : types) {
            String typeKey = String.format(KEY_USER_UNREAD_BY_TYPE, userId, type);
            redisTemplate.delete(typeKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markTypeAsRead(String type, Long userId) {
        int updated = notificationMapper.markTypeAsRead(userId, type);
        log.info("【标记类型已读】userId={}, type={}, count={}", userId, type, updated);

        // 更新 Redis
        String typeKey = String.format(KEY_USER_UNREAD_BY_TYPE, userId, type);
        redisTemplate.delete(typeKey);

        String totalKey = String.format(KEY_USER_UNREAD_COUNT, userId);
        redisTemplate.opsForValue().decrement(totalKey, updated);
    }

    // ========== 删除通知 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);

        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此通知");
        }

        notificationMapper.deleteById(notificationId);

        // 如果是未读通知，更新 Redis
        if (notification.getIsRead() == 0) {
            decrementUnreadCount(userId, notification.getType());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteNotifications(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Notification> notifications = notificationMapper.selectBatchIds(ids);

        // 验证权限
        for (Notification notification : notifications) {
            if (!notification.getUserId().equals(userId)) {
                throw new BusinessException("无权删除部分通知");
            }
        }

        // 统计未读数量（用于更新 Redis）
        Map<String, Long> unreadTypeCounts = notifications.stream()
                .filter(n -> n.getIsRead() == 0)
                .collect(Collectors.groupingBy(Notification::getType, Collectors.counting()));

        notificationMapper.deleteBatchIds(ids);

        // 更新 Redis
        long totalUnread = unreadTypeCounts.values().stream().mapToLong(Long::longValue).sum();
        if (totalUnread > 0) {
            String totalKey = String.format(KEY_USER_UNREAD_COUNT, userId);
            redisTemplate.opsForValue().decrement(totalKey, totalUnread);

            unreadTypeCounts.forEach((type, count) -> {
                String typeKey = String.format(KEY_USER_UNREAD_BY_TYPE, userId, type);
                redisTemplate.opsForValue().decrement(typeKey, count);
            });
        }

        log.info("【批量删除通知】userId={}, count={}", userId, ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearReadNotifications(Long userId) {
        int deleted = notificationMapper.deleteReadNotifications(userId);
        log.info("【清空已读通知】userId={}, count={}", deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanOldNotifications(Long userId, Integer days) {
        int deleted = notificationMapper.deleteOldReadNotifications(userId, days);
        log.info("【清理历史通知】userId={}, days={}, count={}", userId, days, deleted);
    }

    // ========== 私有辅助方法 ==========

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
    }

    /**
     * 减少未读计数（Redis）
     */
    private void decrementUnreadCount(Long userId, String type) {
        String totalKey = String.format(KEY_USER_UNREAD_COUNT, userId);
        String typeKey = String.format(KEY_USER_UNREAD_BY_TYPE, userId, type);

        redisTemplate.opsForValue().decrement(totalKey, 1);
        redisTemplate.opsForValue().decrement(typeKey, 1);
    }

    /**
     * 转换为 VO
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);

        if (notification.getSenderId() != null && notification.getSender() != null) {
            UserSimpleVO sender = new UserSimpleVO();
            sender.setId(notification.getSender().getId());
            sender.setUsername(notification.getSender().getUsername());
            sender.setNickname(notification.getSender().getNickname());
            sender.setAvatarUrl(notification.getSender().getAvatarUrl());
            vo.setSender(sender);
        }

        vo.setTypeText(getTypeText(notification.getType()));
        vo.setRelativeTime(getRelativeTime(notification.getCreatedAt()));

        return vo;
    }

    private String getTypeText(String type) {
        return switch (type) {
            case "COMMENT" -> "评论";
            case "REPLY" -> "回复";
            case "LIKE" -> "点赞";
            case "LIKE_COMMENT" -> "评论点赞";
            case "FAVORITE" -> "收藏";
            case "FOLLOW" -> "关注";
            case "SYSTEM" -> "系统通知";
            default -> "未知";
        };
    }

    private String getRelativeTime(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "刚刚";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "分钟前";
        long hours = minutes / 60;
        if (hours < 24) return hours + "小时前";
        long days = hours / 24;
        if (days < 30) return days + "天前";
        long months = days / 30;
        if (months < 12) return months + "个月前";
        return (months / 12) + "年前";
    }
}