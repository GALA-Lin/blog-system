package com.blog.module.comment.controller;

import com.blog.DTO.comment.*;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.module.comment.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.blog.util.SecurityUtil;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-15:21
 * @Description:
 */
@Tag(name = "评论模块", description = "评论模块相关接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final ICommentService commentService;

    @Operation(summary = "创建评论")
    @PostMapping
    @PreAuthorize("hasAuthority('comment:create')")
    public Result<CommentDTO> createComment(
            @Valid @RequestBody CommentCreateDTO dto,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        CommentDTO comment = commentService.createComment(dto, userId, ipAddress, userAgent);
        return Result.success(comment);
    }

    @Operation(summary = "更新评论")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('comment:update:own')")
    public Result<CommentDTO> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateDTO dto) {
        dto.setId(id);
        Long userId = SecurityUtil.getCurrentUserId();

        CommentDTO comment = commentService.updateComment(dto, userId);
        return Result.success(comment);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('comment:delete:own')")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        commentService.deleteComment(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取评论详情")
    @GetMapping("/{id}")
    public Result<CommentDTO> getCommentDetail(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserIdOrNull();
        CommentDTO comment = commentService.getCommentDetail(id, userId);
        return Result.success(comment);
    }

    @Operation(summary = "获取文章评论", description = "使用分页获取帖子评论")
    @GetMapping("/post/{postId}")
    public Result<PageResult<CommentDTO>> getPostComments(
            @PathVariable Long postId,
            @Valid CommentQueryDTO queryDTO) {
        queryDTO.setPostId(postId);
        Long userId = SecurityUtil.getCurrentUserIdOrNull();

        PageResult<CommentDTO> comments = commentService.getPostComments(queryDTO, userId);
        return Result.success(comments);
    }

    @Operation(summary = "获取评论树", description = "获取帖子的评论树")
    @GetMapping("/post/{postId}/tree")
    public Result<List<CommentDTO>> getCommentTree(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserIdOrNull();
        List<CommentDTO> tree = commentService.getCommentTree(postId, userId);
        return Result.success(tree);
    }

    @Operation(summary = "获取用户评论", description = "使用分页获取用户评论")
    @GetMapping("/user/{userId}")
    public Result<PageResult<CommentDTO>> getUserComments(
            @PathVariable Long userId,
            @Valid CommentQueryDTO queryDTO) {
        queryDTO.setUserId(userId);
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();

        PageResult<CommentDTO> comments = commentService.getUserComments(queryDTO, currentUserId);
        return Result.success(comments);
    }

    @Operation(summary = "获取我的评论")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<CommentDTO>> getMyComments(@Valid CommentQueryDTO queryDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        queryDTO.setUserId(userId);

        PageResult<CommentDTO> comments = commentService.getUserComments(queryDTO, userId);
        return Result.success(comments);
    }

    @Operation(summary = "获取评论数量")
    @GetMapping("/post/{postId}/count")
    public Result<Long> getPostCommentCount(@PathVariable Long postId) {
        Long count = commentService.getPostCommentCount(postId);
        return Result.success(count);
    }

    // ========== Admin APIs ==========

    @Operation(summary = "获得最新评论")
    @GetMapping("/admin/latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public Result<PageResult<CommentDTO>> getLatestComments(@Valid CommentQueryDTO queryDTO) {
        PageResult<CommentDTO> comments = commentService.getLatestComments(queryDTO);
        return Result.success(comments);
    }

    @Operation(summary = "更新评论状态")
    @PutMapping("/admin/status")
    @PreAuthorize("hasAuthority('comment:moderate')")
    public Result<Void> updateCommentStatus(@Valid @RequestBody CommentStatusDTO dto) {
        commentService.updateCommentStatus(dto);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('comment:delete:any')")
    public Result<Void> adminDeleteComment(@PathVariable Long id) {
        commentService.deleteComment(id, null); // Admin can delete any comment
        return Result.success();
    }

    /**
     * 获取客户端IP地址
     * @param request 请求对象
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Take the first IP if multiple IPs are present
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}