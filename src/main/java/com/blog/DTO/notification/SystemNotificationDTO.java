package com.blog.DTO.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 系统通知创建 DTO (管理员使用)
 */
@Data
public class SystemNotificationDTO {

    /**
     * 接收用户ID列表（null表示全体用户）
     */
    private List<Long> userIds;

    @NotBlank(message = "通知标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @Size(max = 500, message = "内容不能超过500个字符")
    private String content;

    /**
     * 相关链接（可选）
     */
    @Size(max = 255, message = "链接不能超过255个字符")
    private String linkUrl;
}
