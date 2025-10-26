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
 * @Date: 2025-10-23-21:43
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteMessage implements Serializable {

    private String messageId;
    private Long userId;
    private Long postId;
    private Long folderId;
    private String action; // FAVORITE, UNFAVORITE
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timestamp;

}
