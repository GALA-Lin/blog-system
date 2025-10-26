package com.blog.DTO.mq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-23-19:17
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeMessage implements Serializable {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID（文章ID或评论ID）
     */
    private Long targetId;

    /**
     * 目标类型：POST, COMMENT
     */
    private String targetType;

    /**
     * 操作类型：LIKE, UNLIKE
     */
    private String action;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timestamp;

    /**
     * 额外数据
     */
    private String extraData;
}
