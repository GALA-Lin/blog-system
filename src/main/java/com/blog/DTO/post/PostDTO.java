package com.blog.DTO.post;

import com.blog.DTO.UserSimpleDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-12-16:16
 * @Description:
 */
@Data
public class PostDTO {

    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String content;

    private String contentType;

    private String coverImage;

    private Integer status; // 0=草稿, 1=已发布, 2=审核中, -1=已删除

    private Integer isTop; // 0=普通, 1=置顶

    private Integer isOriginal; // 1=原创, 0=转载

    private String originalUrl; // 转载来源URL

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer commentCount;

    private Integer allowComment; // 1=允许评论, 0=禁止评论

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 作者信息
     */
    private UserSimpleDTO author;

    /**
     * 分类列表
     */
    private List<CategorySimpleDTO> categories;

    /**
     * 标签列表
     */
    private List<TagSimpleDTO> tags;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏
     */
    private Boolean isFavorited;

    /**
     * 当前用户收藏到的收藏夹ID
     */
    private Long favoriteFolderId;
}

