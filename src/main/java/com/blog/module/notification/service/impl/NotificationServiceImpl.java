package com.blog.module.notification.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.DTO.notification.NotificationQueryDTO;
import com.blog.DTO.notification.SystemNotificationDTO;
import com.blog.VO.auth.UserSimpleVO;
import com.blog.VO.notification.NotificationStatVO;
import com.blog.VO.notification.NotificationVO;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.entity.Notification;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.module.notification.mapper.NotificationMapper;
import com.blog.module.notification.service.INotificationService;
import com.blog.module.post.mapper.PostMapper;
import com.blog.util.SecurityUtil;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-21:20
 * @Description:
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    /**
     * 创建评论通知
     * @param recipientId 接收者ID（文章作者）
     * @param senderId 发送者ID（评论者）
     * @param commentId 评论ID
     * @param postId 文章ID
     */
    @Override
    @Async
    public void createCommentNotification(Long recipientId, Long senderId, Long commentId, Long postId) {
        // 不给自己发通知
        if (recipientId.equals(senderId)) {
            return;
        }

        // 防止重复通知
        if (Boolean.TRUE.equals(notificationMapper.existsSimilarNotification(
                recipientId, senderId, "COMMENT", commentId))) {
            log.debug("跳过重复通知: type=COMMENT, commentId={}", commentId);
            return;
        }

        User sender = userMapper.selectById(senderId);
        Post post = postMapper.selectById(postId);

        if (sender == null || post == null) {
            log.warn("创建评论通知失败: sender={}, post={}", senderId, postId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setSenderId(senderId);
        notification.setType("COMMENT");
        notification.setTitle("新评论通知");
        notification.setContent(String.format("%s 评论了您的文章《%s》",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername(),
                post.getTitle()));
        notification.setLinkUrl("/posts/" + postId + "#comment-" + commentId);
        notification.setRelatedId(commentId);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        log.info("创建评论通知成功: recipientId={}, senderId={}, commentId={}",
                recipientId, senderId, commentId);
    }

    /**
     * 创建回复通知
     * @param recipientId 接收者ID（被回复的用户）
     * @param senderId 发送者ID（回复者）
     * @param commentId 评论ID
     * @param postId 文章ID
     */
    @Override
    @Async
    public void createReplyNotification(Long recipientId, Long senderId, Long commentId, Long postId) {
         // 不给自己发通知
        if (recipientId.equals(senderId)) {
            return;
        }
        // 防止重复通知
        if (Boolean.TRUE.equals(notificationMapper.existsSimilarNotification(
                recipientId, senderId, "REPLY", commentId))) {
            return;
        }

        User sender = userMapper.selectById(senderId);
        Post post = postMapper.selectById(postId);
        if (sender == null || post == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setSenderId(senderId);
        notification.setType("REPLY");
        notification.setTitle("新回复通知");
        notification.setContent(String.format("%s 回复了您的评论",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername()));
        notification.setLinkUrl("/posts/" + postId + "#comment-" + commentId);
        notification.setRelatedId(commentId);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        log.info("创建回复通知成功: recipientId={}, senderId={}, commentId={}",
                recipientId, senderId, commentId);
    }


    @Override
    public void createLikeNotification(Long recipientId, Long senderId, Long relatedId) {
        // 不给自己发通知
        if (recipientId.equals(senderId)) {
            return;
        }
        // 防止重复通知
        if (Boolean.TRUE.equals(notificationMapper.existsSimilarNotification(
                recipientId, senderId, "LIKE", relatedId))) {
            return;
        }

        User sender = userMapper.selectById(senderId);
        if (sender == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setSenderId(senderId);
        notification.setType("LIKE");
        notification.setTitle("新点赞通知");
        notification.setContent(String.format("%s 赞了您的文章",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername()));
        notification.setLinkUrl("");
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        log.info("创建点赞通知成功: recipientId={}, senderId={}, relatedId={}",
                recipientId, senderId, relatedId);
    }

    @Override
    public void createFavoriteNotification(Long recipientId, Long senderId, Long postId) {
        // 不给自己发通知
        if (recipientId.equals(senderId)) {
            return;
        }
        // 防止重复通知
        if (Boolean.TRUE.equals(notificationMapper.existsSimilarNotification(
                recipientId, senderId, "FAVORITE", postId))) {
            return;
        }

        User sender = userMapper.selectById(senderId);
        Post post = postMapper.selectById(postId);
        if (sender == null || post == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setSenderId(senderId);
        notification.setType("FAVORITE");
        notification.setTitle("新收藏通知");
        notification.setContent(String.format("%s 收藏了您的文章《%s》",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername(),
                post.getTitle()));
        notification.setLinkUrl("/posts/" + postId);
        notification.setRelatedId(postId);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        log.info("创建收藏通知成功: recipientId={}, senderId={}, postId={}",
                recipientId, senderId, postId);

    }

    @Override
    public void createFollowNotification(Long recipientId, Long senderId) {
        // 不给自己发通知
        if (recipientId.equals(senderId)) {
            return;
        }
        // 防止重复通知
        if (Boolean.TRUE.equals(notificationMapper.existsSimilarNotification(
                recipientId, senderId, "FOLLOW", null))) {
            return;
        }

        User sender = userMapper.selectById(senderId);
        if (sender == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setSenderId(senderId);
        notification.setType("FOLLOW");
        notification.setTitle("新关注通知");
        notification.setContent(String.format("%s 关注了你",
                sender.getNickname() != null ? sender.getNickname() : sender.getUsername()));
        notification.setLinkUrl("");
        notification.setRelatedId(null);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        log.info("创建关注通知成功: recipientId={}, senderId={}", recipientId, senderId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSystemNotification(SystemNotificationDTO dto) {
        // 新增：校验发送者合法性（必须有发送者，且发送者存在）
        Long senderId = SecurityUtil.getCurrentUserId();
        if (senderId == null) {
            throw new BusinessException("创建系统通知失败：发送者ID不能为空");
        }
        User sender = userMapper.selectById(senderId);
        if (sender == null) {
            log.error("创建系统通知失败：发送者不存在（senderId={}）", senderId);
            throw new BusinessException("创建系统通知失败：发送者不存在");
        }

        // 原有逻辑：根据接收者列表（或全体用户）创建通知，新增传递senderId
        if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            // 全体用户通知
            List<User> allUsers = userMapper.selectList(null);
            for (User user : allUsers) {
                createSystemNotificationForUser(user.getId(), dto, senderId);
            }
        } else {
            // 指定用户通知
            for (Long userId : dto.getUserIds()) {
                if (userId.equals(senderId)) {
                    continue;
                }
                createSystemNotificationForUser(userId, dto, senderId);
            }
        }

        log.info("创建系统通知成功: senderId={}, userCount={}, title={}",
                senderId,
                dto.getUserIds() != null ? dto.getUserIds().size() : "全体用户",
                dto.getTitle());
    }

    /**
     * 创建系统通知
     * @param userId 接收者ID
     * @param dto 系统通知DTO
     */
    private void createSystemNotificationForUser(Long userId, SystemNotificationDTO dto,Long senderId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setSenderId(senderId);
        notification.setType("SYSTEM");
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setLinkUrl(dto.getLinkUrl());
        notification.setIsRead(0);

        notificationMapper.insert(notification);
    }
    // ========== 查询通知 ==========

    /**
     * 获取用户通知
     * @param queryDTO 查询条件
     * @param userId 用户ID
     * @return 通知响应VO
     */
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

    /**
     * 获取通知详情
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return
     */
    @Override
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
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }

        return convertToVO(notification);
    }

    /**
     * 获取未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    /**
     * 获取通知统计信息
     * @param userId 用户ID
     * @return 通知统计信息
     */
    @Override
    public NotificationStatVO getNotificationStats(Long userId) {
        NotificationStatVO stats = new NotificationStatVO();
        // 未读通知数量
        stats.setUnreadCount(notificationMapper.countUnread(userId));
        // 未读通知类型统计
        List<Map<String, Object>> typeStats = notificationMapper.countUnreadByType(userId);

        // 转换为Map
        Map<String, Long> typeCountMap = new HashMap<>();
        for (Map<String, Object> stat : typeStats) {
            String type = (String) stat.get("type");
            Long count = ((Number) stat.get("count")).longValue();
            typeCountMap.put(type, count);
        }

        stats.setUnreadCommentCount(typeCountMap.getOrDefault("COMMENT", 0L));
        stats.setUnreadReplyCount(typeCountMap.getOrDefault("REPLY", 0L));
        stats.setUnreadLikeCount(typeCountMap.getOrDefault("LIKE", 0L));
        stats.setUnreadFavoriteCount(typeCountMap.getOrDefault("FAVORITE", 0L));
        stats.setUnreadFollowCount(typeCountMap.getOrDefault("FOLLOW", 0L));
        stats.setUnreadSystemCount(typeCountMap.getOrDefault("SYSTEM", 0L));

        return stats;
    }
    // ========== 标记已读 ==========

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
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
        }
    }

    /**
     * 批量标记通知为已读
     * @param ids 通知ID列表
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        int updated = notificationMapper.batchMarkAsRead(userId, ids);
        log.info("批量标记已读: userId={}, count={}", userId, updated);
    }

    /**
     * 标记全部通知为已读
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        int updated = notificationMapper.markAllRead(userId);
        log.info("标记全部已读: userId={}, count={}", userId, updated);
    }

    /**
     * 标记指定类型通知为已读
     * @param type 通知类型
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markTypeAsRead(String type, Long userId) {
        int updated = notificationMapper.markTypeAsRead(userId, type);
        log.info("标记类型已读: userId={}, type={}, count={}", userId, type, updated);
    }

    // ========== 删除通知 ==========

    /**
     * 删除通知
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
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
    }

    /**
     * 批量删除通知
     * @param ids 通知ID列表
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteNotifications(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 验证所有通知都属于当前用户
        List<Notification> notifications = notificationMapper.selectBatchIds(ids);
        for (Notification notification : notifications) {
            if (!notification.getUserId().equals(userId)) {
                throw new BusinessException("无权删除部分通知");
            }
        }

        notificationMapper.deleteBatchIds(ids);
        log.info("批量删除通知: userId={}, count={}", userId, ids.size());
    }

    /**
     * 清空已读通知
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearReadNotifications(Long userId) {
        int deleted = notificationMapper.deleteReadNotifications(userId);
        log.info("清空已读通知: userId={}, count={}", userId, deleted);
    }

    /**
     * 清理历史通知
     * @param userId 用户ID
     * @param days 天数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanOldNotifications(Long userId, Integer days) {
        int deleted = notificationMapper.deleteOldReadNotifications(userId, days);
        log.info("清理历史通知: userId={}, days={}, count={}", userId, days, deleted);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 转换通知实体为 VO
     * @param notification 通知实体
     * @return 通知 VO
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);

        // 转换发送者信息
        if (notification.getSenderId() != null && notification.getSender() != null) {
            UserSimpleVO sender = new UserSimpleVO();
            sender.setId(notification.getSender().getId());
            sender.setUsername(notification.getSender().getUsername());
            sender.setNickname(notification.getSender().getNickname());
            sender.setAvatarUrl(notification.getSender().getAvatarUrl());
            vo.setSender(sender);
        }

        // 设置类型显示文本
        vo.setTypeText(getTypeText(notification.getType()));

        // 设置相对时间
        vo.setRelativeTime(getRelativeTime(notification.getCreatedAt()));

        return vo;
    }

    /**
     * 获取通知类型显示文本
     * @param type 通知类型
     * @return 通知类型显示文本
     */
    private String getTypeText(String type) {
        return switch (type) {
            case "COMMENT" -> "评论";
            case "REPLY" -> "回复";
            case "LIKE" -> "点赞";
            case "FAVORITE" -> "收藏";
            case "FOLLOW" -> "关注";
            case "SYSTEM" -> "系统通知";
            default -> "未知";
        };
    }

    /**
     * 获取相对时间（如：3分钟前）
     * @param dateTime 时间
     * @return 相对时间
     */
    private String getRelativeTime(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());

        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return "刚刚";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "分钟前";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "小时前";
        }

        long days = hours / 24;
        if (days < 30) {
            return days + "天前";
        }

        long months = days / 30;
        if (months < 12) {
            return months + "个月前";
        }

        return (months / 12) + "年前";
    }
}
