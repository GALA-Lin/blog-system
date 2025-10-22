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


/**
 * 通知服务接口
 */
public interface INotificationService {

    // ========== 创建通知 ==========

    /**
     * 创建评论通知
     * @param recipientId 接收者ID（文章作者）
     * @param senderId 发送者ID（评论者）
     * @param commentId 评论ID
     * @param postId 文章ID
     */
    void createCommentNotification(Long recipientId, Long senderId, Long commentId, Long postId);

    /**
     * 创建回复通知
     * @param recipientId 接收者ID（被回复的用户）
     * @param senderId 发送者ID（回复者）
     * @param commentId 评论ID
     * @param postId 文章ID
     */
    void createReplyNotification(Long recipientId, Long senderId, Long commentId, Long postId);

    /**
     * 创建点赞通知
     * @param recipientId 接收者ID（文章/评论作者）
     * @param senderId 发送者ID（点赞者）
     * @param relatedId 相关ID（文章ID或评论ID）
     */
    void createLikeNotification(Long recipientId, Long senderId, Long relatedId);

    /**
     * 创建收藏通知
     * @param recipientId 接收者ID（文章作者）
     * @param senderId 发送者ID（收藏者）
     * @param postId 文章ID
     */
    void createFavoriteNotification(Long recipientId, Long senderId, Long postId);

    /**
     * 创建关注通知
     * @param recipientId 接收者ID（被关注的用户）
     * @param senderId 发送者ID（关注者）
     */
    void createFollowNotification(Long recipientId, Long senderId);

    /**
     * 创建系统通知
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
     * 获取未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 获取通知统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    NotificationStatVO getNotificationStats(Long userId);

    // ========== 标记已读 ==========

    /**
     * 标记通知为已读
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