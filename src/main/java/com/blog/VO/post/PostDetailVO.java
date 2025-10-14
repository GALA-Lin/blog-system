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
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;

    private String content;
    private Long userId;
    private String authorName;
    private String authorAvatar;

    private Integer status;
    private Integer commentCount;

    private List<CategoryVO> categories;
    private List<TagVO> tags;

    private LocalDateTime publishedAt;
}

