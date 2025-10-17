package com.blog.DTO.notification;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 标记已读 DTO
 */
@Data
public class NotificationReadDTO {

    /**
     * 通知ID列表
     */
    @NotEmpty(message = "通知ID列表不能为空")
    private List<Long> ids;
}
