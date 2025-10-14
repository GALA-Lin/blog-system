package com.blog.VO.post;

import com.blog.VO.auth.UserSimpleDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章简要信息 DTO
 */
@Data
public class PostSimpleVO {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private UserSimpleDTO author;
}
