package com.blog.DTO.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-23-21:45
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountSyncMessage implements Serializable {

    private String messageId;
    private String type; // LIKE_POST, LIKE_COMMENT, FAVORITE, COMMENT
    private Long targetId;
    private Integer delta; // 增量：+1 或 -1
    private LocalDateTime timestamp;

}