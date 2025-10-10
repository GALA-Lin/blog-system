package com.blog.DTO.comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-16:27
 * @Description: 评论状态更新 DTO (管理员)
 */
@Data
public class CommentStatusDTO {

    @NotEmpty(message = "评论ID不能为空")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    @Min(value = -2, message = "无效的状态")
    @Max(value = 1, message = "无效的状态")
    private Integer status;

    /**
     * 状态变更原因（可选）
     */
    private String reason;
}