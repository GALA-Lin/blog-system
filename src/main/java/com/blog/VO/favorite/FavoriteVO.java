package com.blog.VO.favorite;

import com.blog.DTO.favorite.FolderSimpleDTO;
import com.blog.VO.post.PostSimpleVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:29
 * @Description:
 */

@Data
public class FavoriteVO {

    private Long id;
    private Long userId;
    private Long postId;
    private Long folderId;
    private String notes;
    private LocalDateTime createdAt;

    /**
     * 文章信息
     */
    private PostSimpleVO post;

    /**
     * 收藏夹信息
     */
    private FolderSimpleDTO folder;
}

