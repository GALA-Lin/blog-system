package com.blog.DTO.notification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-17-21:55
 * @Description:
 */
@Data
public class NotificationQueryDTO {

    /**
     * 通知类型：COMMENT, REPLY, LIKE, FAVORITE, FOLLOW, SYSTEM
     */
    private String type;

    /**
     * 是否已读：0=未读, 1=已读, null=全部
     */
    private Integer isRead;

    /**
     * 排序方式：created_at
     */
    private String sortBy = "created_at";

    /**
     * 排序顺序：asc, desc
     */
    private String sortOrder = "desc";

    @Min(value = 1, message = "页码必须 >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页面大小必须 >= 1")
    @Max(value = 100, message = "页面大小必须 <= 100")
    private Integer pageSize = 20;
}

