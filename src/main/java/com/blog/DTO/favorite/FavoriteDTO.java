package com.blog.DTO.favorite;

import com.blog.DTO.post.PostSimpleDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:29
 * @Description:
 */

@Data
public class FavoriteDTO {

    private Long id;
    private Long userId;
    private Long postId;
    private Long folderId;
    private String notes;
    private LocalDateTime createdAt;

    /**
     * 文章信息
     */
    private PostSimpleDTO post;

    /**
     * 收藏夹信息
     */
    private FolderSimpleDTO folder;
}

