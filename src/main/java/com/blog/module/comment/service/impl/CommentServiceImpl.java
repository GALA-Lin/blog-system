package com.blog.module.comment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.DTO.comment.*;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;

import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.module.comment.mapper.CommentMapper;
import com.blog.module.comment.service.ICommentService;
import com.blog.module.notification.service.INotificationService;
import com.blog.module.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comment Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final INotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDTO createComment(CommentCreateDTO dto, Long userId, String ipAddress, String userAgent) {
        // Validate post exists and allows comments
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException("Post not found");
        }
        if (post.getStatus() != 1) {
            throw new BusinessException("Cannot comment on unpublished post");
        }
        if (post.getAllowComment() == 0) {
            throw new BusinessException("Comments are disabled for this post");
        }

        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setIpAddress(ipAddress);
        comment.setUserAgent(userAgent);
        comment.setStatus(1); // Auto-approve, or 0 for manual review
        comment.setLikeCount(0);
        comment.setReplyCount(0);

        // Handle parent comment and reply
        if (dto.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(dto.getParentId());
            if (parentComment == null) {
                throw new BusinessException("Parent comment not found");
            }
            if (!parentComment.getPostId().equals(dto.getPostId())) {
                throw new BusinessException("Parent comment does not belong to this post");
            }

            comment.setParentId(dto.getParentId());
            comment.setRootId(parentComment.getRootId() != null ? parentComment.getRootId() : parentComment.getId());

            if (dto.getReplyToUserId() != null) {
                comment.setReplyToUserId(dto.getReplyToUserId());
            } else {
                comment.setReplyToUserId(parentComment.getUserId());
            }
        }

        // Save comment
        commentMapper.insert(comment);

        // Send notifications (async)
        try {
            // Notify post author
            if (!post.getUserId().equals(userId)) {
                notificationService.createCommentNotification(post.getUserId(), userId, comment.getId(), post.getId());
            }

            // Notify replied user
            if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(userId)) {
                notificationService.createReplyNotification(comment.getReplyToUserId(), userId, comment.getId(), post.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send comment notification", e);
        }

        return getCommentDetail(comment.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDTO updateComment(CommentUpdateDTO dto, Long userId) {
        Comment comment = commentMapper.selectById(dto.getId());
        if (comment == null) {
            throw new BusinessException("Comment not found");
        }

        // Only author can update
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("You can only update your own comments");
        }

        // Cannot update deleted comments
        if (comment.getStatus() == -1) {
            throw new BusinessException("Cannot update deleted comment");
        }

        comment.setContent(dto.getContent());
        commentMapper.updateById(comment);

        return getCommentDetail(comment.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("Comment not found");
        }

        if (!canDeleteComment(commentId, userId)) {
            throw new BusinessException("You don't have permission to delete this comment");
        }

        // Soft delete: update status to -1
        comment.setStatus(-1);
        commentMapper.updateById(comment);

        // Note: Triggers will handle updating counts
    }

    @Override
    public CommentDTO getCommentDetail(Long commentId, Long userId) {
        Comment comment = commentMapper.selectCommentWithAuthor(commentId);
        if (comment == null) {
            throw new BusinessException("Comment not found");
        }

        CommentDTO dto = convertToDTO(comment);

        // Load replies if it's a parent comment
        if (comment.getParentId() == null) {
            List<Comment> replies = commentMapper.selectRepliesByCommentId(commentId, userId);
            dto.setChildren(replies.stream().map(this::convertToDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public PageResult<CommentDTO> getPostComments(CommentQueryDTO queryDTO, Long userId) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Comment> commentPage = commentMapper.selectPostCommentsWithAuthor(
                page,
                queryDTO.getPostId(),
                queryDTO.getStatus() != null ? queryDTO.getStatus() : 1,
                userId
        );

        List<CommentDTO> dtoList = commentPage.getRecords().stream()
                .map(comment -> {
                    CommentDTO dto = convertToDTO(comment);
                    // Load replies if requested
                    if (Boolean.TRUE.equals(queryDTO.getLoadReplies())) {
                        List<Comment> replies = commentMapper.selectRepliesByCommentId(comment.getId(), userId);
                        dto.setChildren(replies.stream().map(this::convertToDTO).collect(Collectors.toList()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, commentPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public List<CommentDTO> getCommentTree(Long postId, Long userId) {
        List<Comment> allComments = commentMapper.selectCommentTree(postId, userId);

        // Build tree structure
        Map<Long, CommentDTO> commentMap = new HashMap<>();
        List<CommentDTO> rootComments = new ArrayList<>();

        // First pass: convert all comments to DTOs
        for (Comment comment : allComments) {
            CommentDTO dto = convertToDTO(comment);
            dto.setChildren(new ArrayList<>());
            commentMap.put(dto.getId(), dto);
        }

        // Second pass: build tree
        for (CommentDTO dto : commentMap.values()) {
            if (dto.getParentId() == null) {
                rootComments.add(dto);
            } else {
                CommentDTO parent = commentMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootComments;
    }

    @Override
    public PageResult<CommentDTO> getUserComments(CommentQueryDTO queryDTO, Long userId) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Comment> commentPage = commentMapper.selectUserComments(page, queryDTO.getUserId(), userId);

        List<CommentDTO> dtoList = commentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, commentPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public PageResult<CommentDTO> getLatestComments(CommentQueryDTO queryDTO) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Comment> commentPage = commentMapper.selectLatestComments(page, queryDTO.getStatus());

        List<CommentDTO> dtoList = commentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, commentPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentStatus(CommentStatusDTO dto) {
        commentMapper.batchUpdateStatus(dto.getIds(), dto.getStatus());

        log.info("Updated {} comments to status: {}, reason: {}",
                dto.getIds().size(), dto.getStatus(), dto.getReason());
    }

    @Override
    public boolean canDeleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // User can delete their own comments
        return comment.getUserId().equals(userId);

        // Check if user is admin or moderator (implement permission check)
        // return hasPermission(userId, "comment:delete:any");
    }

    @Override
    public boolean canUpdateComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // Only author can update
        return comment.getUserId().equals(userId);
    }

    @Override
    public Long getPostCommentCount(Long postId) {
        return commentMapper.countByPostId(postId);
    }

    @Override
    public Long getUserCommentCount(Long userId) {
        return commentMapper.countByUserId(userId);
    }

    /**
     * Convert Comment entity to DTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // Convert author
        if (comment.getAuthor() != null) {
            UserSimpleDTO author = new UserSimpleDTO();
            BeanUtils.copyProperties(comment.getAuthor(), author);
            dto.setAuthor(author);
        }

        // Convert reply-to user
        if (comment.getReplyToUser() != null) {
            UserSimpleDTO replyToUser = new UserSimpleDTO();
            BeanUtils.copyProperties(comment.getReplyToUser(), replyToUser);
            dto.setReplyToUser(replyToUser);
        }

        return dto;
    }
}