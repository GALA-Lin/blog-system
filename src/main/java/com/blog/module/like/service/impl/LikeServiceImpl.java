package com.blog.module.like.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.VO.auth.UserSimpleDTO;
import com.blog.VO.post.PostVO;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.entity.*;
import com.blog.module.comment.mapper.CommentMapper;
import com.blog.module.like.mapper.CommentLikeMapper;
import com.blog.module.like.mapper.PostLikeMapper;
import com.blog.module.like.service.LikeService;
import com.blog.module.notification.service.INotificationService;
import com.blog.module.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-11-13:00
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final PostLikeMapper postLikeMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final INotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likePost(Long postId, Long userId) {
        // 检验文章是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        if (post.getStatus() !=1){
            throw new BusinessException("文章未发布");
        }
        // 检查是否已经点赞
        if (Boolean.TRUE.equals(postLikeMapper.isLikedByUser(postId, userId))) {
            return false; // 已经点赞过
        }
        try{
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            postLikeMapper.insert(postLike);
            postMapper.incrementLikeCount(postId);
            if (!post.getUserId().equals(userId)) {
                try {
                    notificationService.createLikeNotification(post.getUserId(), userId, postId);
                } catch (Exception e) {
                    log.error("发送点赞通知失败", e);
                }
            }

            return true;
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突，说明已点赞
            log.warn("重复点赞: postId={}, userId={}", postId, userId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlikePost(Long postId, Long userId) {
        // 删除点赞记录
        int deleted = postLikeMapper.deleteByPostAndUser(postId, userId);

        if (deleted > 0) {
            postMapper.decrementLikeCount(postId);
            return true;
        }

        return false; // 未点赞过
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean togglePostLike(Long postId, Long userId) {
        if (Boolean.TRUE.equals(isPostLiked(postId, userId))) {
            unlikePost(postId, userId);
            return false; // 取消点赞
        } else {
            likePost(postId, userId);
            return true; // 点赞
        }
    }

    @Override
    public Boolean isPostLiked(Long postId, Long userId) {
        return postLikeMapper.isLikedByUser(postId, userId);
    }

    @Override
    public Map<Long, Boolean> batchCheckPostLikes(List<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> likedPostIds = postLikeMapper.selectLikedPostIdsByUser(postIds, userId);

        Map<Long, Boolean> result = new HashMap<>();
        for (Long postId : postIds) {
            result.put(postId, likedPostIds.contains(postId));
        }

        return result;
    }

    @Override
    public PageResult<UserSimpleDTO> getPostLikeUsers(Long postId, Integer pageNum, Integer pageSize) {
        Page<PostLike> page = new Page<>(pageNum, pageSize);
        IPage<PostLike> likePage = postLikeMapper.selectPostLikesWithUser(page, postId);

        List<UserSimpleDTO> users = likePage.getRecords().stream()
                .map(like -> {
                    UserSimpleDTO dto = new UserSimpleDTO();
                    User user = like.getUser();
                    if (user != null) {
                        dto.setId(user.getId());
                        dto.setUsername(user.getUsername());
                        dto.setNickname(user.getNickname());
                        dto.setAvatarUrl(user.getAvatarUrl());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(users, likePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<PostVO> getUserLikePosts(Long userId, Integer pageNum, Integer pageSize) {
        Page<PostLike> page = new Page<>(pageNum, pageSize);
        IPage<PostLike> likePage = postLikeMapper.selectUserLikesWithPost(page, userId);

        List<PostVO> posts = likePage.getRecords().stream()
                .map(like -> {
                    PostVO dto = new PostVO();
                    Post post = like.getPost();
                    if (post != null) {
                        // 手动映射字段
                        dto.setId(post.getId());
                        dto.setTitle(post.getTitle());
                        dto.setSlug(post.getSlug());
                        dto.setSummary(post.getSummary());
                        dto.setContent(post.getContent());
                        dto.setContentType(post.getContentType());
                        dto.setCoverImage(post.getCoverImage());
                        dto.setStatus(post.getStatus());
                        dto.setIsTop(post.getIsTop());
                        dto.setIsOriginal(post.getIsOriginal());
                        dto.setOriginalUrl(post.getOriginalUrl());
                        dto.setViewCount(Math.toIntExact(post.getViewCount()));
                        dto.setLikeCount(post.getLikeCount());
                        dto.setFavoriteCount(post.getFavoriteCount());
                        dto.setCommentCount(post.getCommentCount());
                        dto.setAllowComment(post.getAllowComment());
                        dto.setPublishedAt(post.getPublishedAt());
                        dto.setCreatedAt(post.getCreatedAt());
                        dto.setUpdatedAt(post.getUpdatedAt());

                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(posts, likePage.getTotal(), pageNum, pageSize);
    }

    // ========== 评论点赞实现 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likeComment(Long commentId, Long userId) {
        // 验证评论是否存在
        Comment comment = commentMapper.selectCommentWithAuthor(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (comment.getStatus() != 1) {
            throw new BusinessException("评论不可用");
        }
        // 检查是否已经点赞
        if (Boolean.TRUE.equals(commentLikeMapper.isLikedByUser(commentId, userId))) {
            return false; // 已经点赞过
        }
        try {
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);
            commentLikeMapper.insert(commentLike);
            commentMapper.incrementLikeCount(commentId);

            // 发送通知（不给自己发）
            if (!comment.getUserId().equals(userId)) {
                try {
                    // 根据实际的通知服务接口调整方法名和参数
                    // 可能的方法名：createCommentLikeNotification 或 createLikeNotification
                    notificationService.createLikeNotification(
                            comment.getUserId(), // 接收通知的用户（评论作者）
                            userId,              // 点赞的用户
                            commentId            // 评论ID
                    );
                } catch (Exception e) {
                    log.error("发送评论点赞通知失败: commentId={}, userId={}", commentId, userId, e);
                }
            }
            return true;
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突，说明已点赞
            log.warn("重复点赞评论: commentId={}, userId={}", commentId, userId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlikeComment(Long commentId, Long userId) {
        // 删除点赞记录
        int deleted = commentLikeMapper.deleteByCommentAndUser(commentId, userId);

        if (deleted > 0) {
            commentMapper.decrementLikeCount(commentId);
            return true;
        }

        return false; // 未点赞过
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleCommentLike(Long commentId, Long userId) {
        if (Boolean.TRUE.equals(isCommentLiked(commentId, userId))) {
            unlikeComment(commentId, userId);
            return false; // 取消点赞
        } else {
            likeComment(commentId, userId);
            return true; // 点赞
        }
    }
    @Override
    public Boolean isCommentLiked(Long commentId, Long userId) {
        return commentLikeMapper.isLikedByUser(commentId, userId);
    }

    @Override
    public Map<Long, Boolean> batchCheckCommentLikes(List<Long> commentIds, Long userId) {
        if (commentIds == null || commentIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> likedCommentIds = commentLikeMapper.selectLikedCommentIdsByUser(commentIds, userId);

        Map<Long, Boolean> result = new HashMap<>();
        for (Long commentId : commentIds) {
            result.put(commentId, likedCommentIds.contains(commentId));
        }

        return result;
    }

    @Override
    public PageResult<UserSimpleDTO> getCommentLikeUsers(Long commentId, Integer pageNum, Integer pageSize) {
        Page<CommentLike> page = new Page<>(pageNum, pageSize);
        IPage<CommentLike> likePage = commentLikeMapper.selectCommentLikesWithUser(page, commentId);

        List<UserSimpleDTO> users = likePage.getRecords().stream()
                .map(like -> {
                    UserSimpleDTO dto = new UserSimpleDTO();
                    User user = like.getUser();
                    if (user != null) {
                        dto.setId(user.getId());
                        dto.setUsername(user.getUsername());
                        dto.setNickname(user.getNickname());
                        dto.setAvatarUrl(user.getAvatarUrl());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(users, likePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<Long> getUserLikeComments(Long userId, Integer pageNum, Integer pageSize) {
        Page<CommentLike> page = new Page<>(pageNum, pageSize);
        IPage<CommentLike> likePage = commentLikeMapper.selectUserLikesWithComment(page, userId);

        List<Long> commentIds = likePage.getRecords().stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toList());

        return new PageResult<>(commentIds, likePage.getTotal(), pageNum, pageSize);
    }
}
