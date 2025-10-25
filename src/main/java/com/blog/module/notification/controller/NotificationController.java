package com.blog.module.notification.controller;

import com.blog.DTO.notification.*;
import com.blog.VO.notification.*;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.module.notification.service.NotificationService;
import com.blog.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-12:08
 * @Description:
 */
@Tag(name = "通知管理", description = "通知相关API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ========== 查询通知 ==========

    @Operation(summary = "获取我的通知列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<NotificationVO>> getMyNotifications(@Valid NotificationQueryDTO queryDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        PageResult<NotificationVO> notifications = notificationService.getUserNotifications(queryDTO, userId);
        return Result.success(notifications);
    }

    @Operation(summary = "获取通知详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<NotificationVO> getNotificationDetail(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        NotificationVO notification = notificationService.getNotificationDetail(id, userId);
        return Result.success(notification);
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getUnreadCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "获取通知统计信息")
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public Result<NotificationStatVO> getNotificationStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        NotificationStatVO stats = notificationService.getNotificationStats(userId);
        return Result.success(stats);
    }

    // ========== 标记已读 ==========

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success("标记成功");
    }

    @Operation(summary = "批量标记为已读")
    @PutMapping("/batch-read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> batchMarkAsRead(@Valid @RequestBody NotificationReadDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.batchMarkAsRead(dto.getIds(), userId);
        return Result.success("批量标记成功");
    }

    @Operation(summary = "标记全部为已读")
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success("全部标记成功");
    }

    @Operation(summary = "标记指定类型为已读")
    @PutMapping("/read-type/{type}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markTypeAsRead(@PathVariable String type) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markTypeAsRead(type, userId);
        return Result.success("标记成功");
    }

    // ========== 删除通知 ==========

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除通知")
    @DeleteMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> batchDeleteNotifications(@RequestBody List<Long> ids) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.batchDeleteNotifications(ids, userId);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "清空已读通知")
    @DeleteMapping("/clear-read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> clearReadNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.clearReadNotifications(userId);
        return Result.success("清空成功");
    }

    @Operation(summary = "清理历史通知（删除30天前的已读通知）")
    @DeleteMapping("/clean-old")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cleanOldNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.cleanOldNotifications(userId, 30);
        return Result.success("清理成功");
    }

    // ========== 管理员接口 ==========

    @Operation(summary = "创建系统通知（管理员）")
    @PostMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createSystemNotification(@Valid @RequestBody SystemNotificationDTO dto) {
        notificationService.createSystemNotification(dto);
        return Result.success("系统通知创建成功");
    }
}