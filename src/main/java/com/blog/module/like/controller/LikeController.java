package com.blog.module.like.controller;

import com.blog.VO.auth.UserSimpleDTO;
import com.blog.VO.post.PostVO;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.module.like.service.LikeService;
import com.blog.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-12-15:59
 * @Description:
 */
@Tag(name = "点赞模块")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // ========== 文章点赞 ==========

    /**
     * 点赞文章
     * @param postId 文章ID
     * @return 成功或失败的提示信息
     */
    @Operation(summary = "点赞文章")
    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> likePost(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean result = likeService.likePost(postId, userId);
        return Result.success((result ? "点赞成功" : "已经点赞过了"), result);
    }

    /**
     * 取消点赞文章
     * @param postId 文章ID
     * @return 成功或失败的提示信息
     */
    @Operation(summary = "取消点赞文章")
    @DeleteMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> unlikePost(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean result = likeService.unlikePost(postId, userId);
        return Result.success((result ? "取消点赞成功" : "没有点赞过"), result);
    }

    /**
     * 切换文章点赞状态
     * @param postId 文章ID
     * @return 成功或失败的提示信息
     */
    @Operation(summary = "切换文章点赞状态", description = "已点赞则取消，未点赞则点赞")
    @PutMapping("/post/{postId}/toggle")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> togglePostLike(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean isLiked = likeService.togglePostLike(postId, userId);
        return Result.success(isLiked ? "点赞成功" : "取消点赞成功" , isLiked);
    }

    /**
     * 检查是否已点赞文章
     * @param postId 文章ID
     * @return 是否已点赞
     */
    @Operation(summary = "检查是否已点赞文章")
    @GetMapping("/post/{postId}/check")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> checkPostLike(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean isLiked = likeService.isPostLiked(postId, userId);
        return Result.success(isLiked);
    }

    /**
     * 批量检查文章点赞状态
     * @param postIds 文章ID列表
     * @return 文章ID与点赞状态的映射
     */
    @Operation(summary = "批量检查文章点赞状态")
    @PostMapping("/posts/batch-check")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<Long, Boolean>> batchCheckPostLikes(@RequestBody List<Long> postIds) {
        Long userId = SecurityUtil.getCurrentUserId();
        Map<Long, Boolean> result = likeService.batchCheckPostLikes(postIds, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取文章的点赞用户列表")
    @GetMapping("/post/{postId}/users")
    public Result<PageResult<UserSimpleDTO>> getPostLikeUsers(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<UserSimpleDTO> users = likeService.getPostLikeUsers(postId, pageNum, pageSize);
        return Result.success(users);
    }

    @Operation(summary = "获取我点赞的文章列表")
    @GetMapping("/my-posts")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<PostVO>> getMyLikePosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        PageResult<PostVO> posts = likeService.getUserLikePosts(userId, pageNum, pageSize);
        return Result.success(posts);
    }

    @Operation(summary = "获取指定用户点赞的文章列表")
    @GetMapping("/user/{userId}/posts")
    public Result<PageResult<PostVO>> getUserLikePosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PostVO> posts = likeService.getUserLikePosts(userId, pageNum, pageSize);
        return Result.success(posts);
    }

    // ========== 评论点赞 ==========

    @Operation(summary = "点赞评论")
    @PostMapping("/comment/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> likeComment(@PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean result = likeService.likeComment(commentId, userId);
        return Result.success(result ? "点赞成功" : "已经点赞过了", result);
    }

    @Operation(summary = "取消点赞评论")
    @DeleteMapping("/comment/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> unlikeComment(@PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean result = likeService.unlikeComment(commentId, userId);
        return Result.success(result ? "取消点赞成功" : "您还没有点赞过", result);
    }

    @Operation(summary = "切换评论点赞状态")
    @PutMapping("/comment/{commentId}/toggle")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> toggleCommentLike(@PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean isLiked = likeService.toggleCommentLike(commentId, userId);
        return Result.success(isLiked ? "点赞成功" : "取消点赞成功", isLiked);
    }

    @Operation(summary = "检查是否已点赞评论")
    @GetMapping("/comment/{commentId}/check")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> checkCommentLike(@PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean isLiked = likeService.isCommentLiked(commentId, userId);
        return Result.success(isLiked);
    }

    @Operation(summary = "批量检查评论点赞状态")
    @PostMapping("/comments/batch-check")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<Long, Boolean>> batchCheckCommentLikes(@RequestBody List<Long> commentIds) {
        Long userId = SecurityUtil.getCurrentUserId();
        Map<Long, Boolean> result = likeService.batchCheckCommentLikes(commentIds, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取评论的点赞用户列表")
    @GetMapping("/comment/{commentId}/users")
    public Result<PageResult<UserSimpleDTO>> getCommentLikeUsers(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<UserSimpleDTO> users = likeService.getCommentLikeUsers(commentId, pageNum, pageSize);
        return Result.success(users);
    }


}
