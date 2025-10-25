package com.blog.module.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-21-16:57
 * @Description:
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询用户通知
     * @param page 分页对象
     * @param userId 用户id
     * @param type 通知类型
     * @param isRead 是否已读
     * @param sortBy 排序字段
     * @param sortOrder 排序方式
     * @return 分页结果
     */
    IPage<Notification> selectUserNotificationsWithSender(
            Page<?> page,
           @Param("userId") Long userId,
           @Param("type") String type,
           @Param("isRead") Integer isRead,
           @Param("sortBy") String sortBy,
           @Param("sortOrder") String sortOrder
    );

    /**
     * 获取未读通知数量
     * @param userId 用户id
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = 0")
    Long countUnread(@Param("userId") Long userId);

    /**
     * 统计指定类型的未读通知数量
     * @param userId 用户ID
     * @param type 通知类型
     * @return 未读数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND type = #{type} AND is_read = 0")
    Long countUnreadByType(@Param("userId") Long userId, @Param("type") String type);
    /**
     * 按类型分组统计未读通知（用于统计页面）
     * @param userId 用户ID
     * @return 各类型未读数量列表
     */
    @Select("SELECT type, COUNT(*) as count FROM notifications " +
            "WHERE user_id = #{userId} AND is_read = 0 " +
            "GROUP BY type")
    List<Map<String, Object>> countUnreadGroupByType(@Param("userId") Long userId);

    /**
     * 全部标记已读
     * @param userId 用户id
     * @return 影响行数
     */
    @Update("UPDATE notifications SET is_read = 1 WHERE user_id = #{userId}")
    int markAllRead(@Param("userId") Long userId);

    /**
     * 批量标记已读
     * @param userId 用户id
     * @param ids 通知id列表
     * @return 影响行数
     */
    @Update({
            "<script>",
            "UPDATE notifications SET is_read = 1 ",
            "WHERE user_id = #{userId} AND id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int batchMarkAsRead(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    /**
     * 标记指定类型的通知为已读
     * @param userId 用户id
     * @param type 通知类型
     * @return 影响行数
     */
    @Update("UPDATE notifications SET is_read = 1 " +
            "WHERE user_id = #{userId} AND type = #{type} AND is_read = 0")
    int markTypeAsRead(@Param("userId") Long userId, @Param("type") String type);
    /**
     * 删除已读通知（清理历史）
     * @param userId 用户id
     * @return 影响行数
     */
    @Delete("DELETE FROM notifications " +
            "WHERE user_id = #{userId} AND is_read = 1")
    int deleteReadNotifications(@Param("userId") Long userId);

    /**
     * 删除指定时间之前的已读通知
     * @param userId 用户id
     * @param days 天数
     * @return 影响行数
     */
    @Delete("DELETE FROM notifications " +
            "WHERE user_id = #{userId} AND is_read = 1 " +
            "AND created_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOldReadNotifications(@Param("userId") Long userId, @Param("days") Integer days);

    /**
     * 检查是否存在相同的通知（防止重复）
     */
    @Select("SELECT COUNT(*) > 0 FROM notifications " +
            "WHERE user_id = #{userId} AND sender_id = #{senderId} " +
            "AND type = #{type} AND related_id = #{relatedId} " +
            "AND created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)")
    Boolean existsSimilarNotification(
            @Param("userId") Long userId,
            @Param("senderId") Long senderId,
            @Param("type") String type,
            @Param("relatedId") Long relatedId
    );
}
