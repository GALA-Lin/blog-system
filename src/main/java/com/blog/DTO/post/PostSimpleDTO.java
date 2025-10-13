package com.blog.DTO.post;

import com.blog.DTO.UserSimpleDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章简要信息 DTO
 */
@Data
public class PostSimpleDTO {
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
