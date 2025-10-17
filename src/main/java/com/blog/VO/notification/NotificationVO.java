package com.blog.VO.notification;

import com.blog.VO.auth.UserSimpleVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-17-21:57
 * @Description: 通知响应VO
 */
@Data
public class NotificationVO {

    private Long id;
    private Long userId;
    private Long senderId;
    private String type;
    private String title;
    private String content;
    private String linkUrl;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createdAt;

    /**
     * 发送者信息
     */
    private UserSimpleVO sender;

    /**
     * 通知类型显示文本
     */
    private String typeText;

    /**
     * 相对时间（如：3分钟前）
     */
    private String relativeTime;
}