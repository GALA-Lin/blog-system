package com.blog.DTO.comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-17:41
 * @Description:
 */
@Data
public class CommentQueryDTO {

    private Long postId;
    private Long userId;
    private Integer status;

    /**
     * 排序字段
     */
    private String sortBy = "created_at";

    /**
     * 排序方式
     * asc: 升序
     * desc: 降序
     */
    private String sortOrder = "desc";


    @Min(value = 1, message = "页码 >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页面大小 >= 1")
    @Max(value = 100, message = "页面大小 <= 100")
    private Integer pageSize = 10;

    /**
     * 是否加载评论的回复
     */
    private Boolean loadReplies = true;
}
