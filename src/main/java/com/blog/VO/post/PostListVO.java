package com.blog.VO.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:33
 * @Description:
 */
@Data
public class PostListVO {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverImage;

    private String authorName;
    private String authorAvatar;

    private Integer status;
    private Long viewCount;
    private Integer likeCount;
    private Integer commentCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
}
