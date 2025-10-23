package com.blog.DTO.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-23-21:44
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage implements Serializable {

    private String messageId;
    private Long recipientId;
    private Long senderId;
    private String type; // LIKE, COMMENT, REPLY, FAVORITE, FOLLOW
    private Long relatedId;
    private String content;
    private LocalDateTime timestamp;
}
