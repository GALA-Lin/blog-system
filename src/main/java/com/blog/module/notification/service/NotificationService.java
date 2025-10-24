package com.blog.module.notification.service;

import com.blog.DTO.notification.NotificationQueryDTO;
import com.blog.DTO.notification.SystemNotificationDTO;
import com.blog.VO.notification.NotificationStatVO;
import com.blog.VO.notification.NotificationVO;
import com.blog.common.PageResult;

import java.util.List;


/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-21:19
 * @Description:
 */

public interface NotificationService {

    // ========== 创建通知（通过 MQ 异步处理）==========

    /**
     * 发送评论通知（异步）
     * @param recipientId 接收者ID（文章作者）
     * @param senderId 发送者ID（评论者）
     * @param commentId 评论ID
     * @param postId 文章ID
     */
    void sendCommentNotification(Long recipientId, Long senderId, Long commentId, Long postId);

    /**
     * 发送回复通知（异步）
     * @param recipientId 接收者ID（被回复的用户）
     * @param senderId 发送者ID（回复者）
     * @param commentId 评论ID
     * @param postId 文章ID
     */
    void sendReplyNotification(Long recipientId, Long senderId, Long commentId, Long postId);

    /**
     * 发送点赞通知（异步）
     * @param recipientId 接收者ID（文章/评论作者）
     * @param senderId 发送者ID（点赞者）
     * @param relatedId 相关ID（文章ID或评论ID）
     * @param type 点赞类型：LIKE, LIKE_COMMENT
     */
    void sendLikeNotification(Long recipientId, Long senderId, Long relatedId, String type);

    /**
     * 发送收藏通知（异步）
     * @param recipientId 接收者ID（文章作者）
     * @param senderId 发送者ID（收藏者）
     * @param postId 文章ID
     */
    void sendFavoriteNotification(Long recipientId, Long senderId, Long postId);

    /**
     * 发送关注通知（异步）
     * @param recipientId 接收者ID（被关注的用户）
     * @param senderId 发送者ID（关注者）
     */
    void sendFollowNotification(Long recipientId, Long senderId);

    /**
     * 创建系统通知（管理员使用）
     * @param dto 系统通知DTO
     */
    void createSystemNotification(SystemNotificationDTO dto);

    // ========== 查询通知 ==========

    /**
     * 获取用户通知列表
     * @param queryDTO 查询条件
     * @param userId 用户ID
     * @return 分页通知列表
     */
    PageResult<NotificationVO> getUserNotifications(NotificationQueryDTO queryDTO, Long userId);

    /**
     * 获取通知详情
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 通知详情
     */
    NotificationVO getNotificationDetail(Long notificationId, Long userId);

    /**
     * 获取未读通知数量（优先从 Redis 获取）
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 获取通知统计信息（Redis + DB）
     * @param userId 用户ID
     * @return 统计信息
     */
    NotificationStatVO getNotificationStats(Long userId);

    // ========== 标记已读 ==========

    /**
     * 标记通知为已读（Redis + DB）
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记为已读
     * @param ids 通知ID列表
     * @param userId 用户ID
     */
    void batchMarkAsRead(List<Long> ids, Long userId);

    /**
     * 标记全部为已读
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 标记指定类型的通知为已读
     * @param type 通知类型
     * @param userId 用户ID
     */
    void markTypeAsRead(String type, Long userId);

    // ========== 删除通知 ==========

    /**
     * 删除通知
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * 批量删除通知
     * @param ids 通知ID列表
     * @param userId 用户ID
     */
    void batchDeleteNotifications(List<Long> ids, Long userId);

    /**
     * 清空已读通知
     * @param userId 用户ID
     */
    void clearReadNotifications(Long userId);

    /**
     * 清理历史通知（删除指定天数之前的已读通知）
     * @param userId 用户ID
     * @param days 天数
     */
    void cleanOldNotifications(Long userId, Integer days);
}