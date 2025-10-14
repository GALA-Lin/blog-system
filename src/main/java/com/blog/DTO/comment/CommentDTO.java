package com.blog.DTO.comment;

import com.blog.VO.auth.UserSimpleVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-16:25
 * @Description:
 */
@Data
public class CommentDTO {

    private Long id;
    private Long postId;
    private String postTitle;
    private Long userId;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private String content;
    private Integer status;
    private Integer likeCount;
    private Integer replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Author information
     */
    private UserSimpleVO author;

    /**
     * Reply target user
     */
    private UserSimpleVO replyToUser;

    /**
     * Child comments (replies)
     */
    private List<CommentDTO> children;

    /**
     * Whether current user has liked
     */
    private Boolean isLiked;

    /**
     * Whether current user is the author
     */
    private Boolean isAuthor;
}
