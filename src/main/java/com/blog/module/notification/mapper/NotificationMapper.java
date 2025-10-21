package com.blog.module.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Notification;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-21-16:57
 * @Description:
 */
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
     * 按类型统计未读通知数量
     * @param userId 用户id
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = 0 GROUP BY type")
    Long countUnreadByType(@Param("userId") Long userId);

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
    @Update(
            "<script>",
            "UPDATE notifications SET is_read = 1 ",
            "WHERE user_id = #{userId} AND id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    )
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
}
