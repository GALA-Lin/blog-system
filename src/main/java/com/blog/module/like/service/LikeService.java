package com.blog.module.like.service;

import com.blog.VO.auth.UserSimpleVO;
import com.blog.VO.post.PostVO;
import com.blog.common.PageResult;

import java.util.List;
import java.util.Map;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-11-02:01
 * @Description:
 */
public interface LikeService {
    // ========== 文章点赞 ==========
    /**
     * 点赞文章
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true=点赞成功, false=已经点赞过
     */
    Boolean likePost(Long postId, Long userId);

    /**
     * 取消点赞文章
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true=取消点赞成功, false=没有点赞过
     */
    Boolean unlikePost(Long postId, Long userId);

    /**
     * 切换文章点赞状态（已点赞则取消，未点赞则点赞）
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true=点赞, false=取消点赞
     */
    Boolean togglePostLike(Long postId, Long userId);

    /**
     * 检查用户是否已点赞文章
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true=已点赞, false=未点赞
     */
    Boolean isPostLiked(Long postId, Long userId);

    /**
     * 批量检查用户是否点赞了多个文章
     * @param postIds 文章ID列表
     * @param userId 用户ID
     * @return Map<文章ID, 是否点赞>
     */
    Map<Long, Boolean> batchCheckPostLikes(List<Long> postIds, Long userId);

    /**
     * 获取文章的点赞用户列表
     * @param postId 文章ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 点赞用户列表
     */
    PageResult<UserSimpleVO> getPostLikeUsers(Long postId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的点赞文章列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 点赞文章列表
     */
    PageResult<PostVO> getUserLikePosts(Long userId, Integer pageNum, Integer pageSize);

    // ========== 评论点赞 ==========
    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return true=点赞成功, false=已经点赞过
     */
    Boolean likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return true=取消点赞成功, false=没有点赞过
     */
    Boolean unlikeComment(Long commentId, Long userId);

    /**
     * 切换评论点赞状态（已点赞则取消，未点赞则点赞）
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return true=点赞, false=取消点赞
     */
    Boolean toggleCommentLike(Long commentId, Long userId);

    /**
     * 检查用户是否已点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return true=已点赞, false=未点赞
     */
    Boolean isCommentLiked(Long commentId, Long userId);

    /**
     * 批量检查用户是否点赞了多个评论
     * @param commentIds 评论ID列表
     * @param userId 用户ID
     * @return Map<评论ID, 是否点赞>
     */
    Map<Long, Boolean> batchCheckCommentLikes(List<Long> commentIds, Long userId);

    /**
     * 获取评论的点赞用户列表
     * @param commentId 评论ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 点赞用户列表
     */
    PageResult<UserSimpleVO> getCommentLikeUsers(Long commentId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的点赞评论列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 点赞评论列表
     */
    PageResult<Long> getUserLikeComments(Long userId, Integer pageNum, Integer pageSize);
}
