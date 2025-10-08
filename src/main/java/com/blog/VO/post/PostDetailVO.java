package com.blog.VO.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:32
 * @Description:
 */
@Data
public class PostDetailVO {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverImage;

    private Long userId;
    private String authorName;
    private String authorAvatar;

    private Integer status;
    private Long viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;

    private List<CategoryVO> categories;
    private List<TagVO> tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
}

