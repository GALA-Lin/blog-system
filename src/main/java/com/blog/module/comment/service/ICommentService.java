package com.blog.module.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.DTO.comment.*;
import com.blog.common.PageResult;
import com.blog.entity.Comment;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-16:18
 * @Description:
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 创建评论
     * @param dto 评论创建DTO
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param userAgent 用户代理，识别用户
     * @return 评论DTO
     */
    CommentDTO createComment(CommentCreateDTO dto, Long userId, String ipAddress, String userAgent);

    /**
     * 更新评论
     * @param dto 评论更新DTO
     * @param userId 用户ID
     * @return 评论DTO
     */
    CommentDTO updateComment(CommentUpdateDTO dto, Long userId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取评论详情
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 评论DTO
     */
    CommentDTO getCommentDetail(Long commentId, Long userId);

    /**
     * 获取文章评论列表
     * @param queryDTO 查询条件DTO
     * @param userId 用户ID
     * @return 评论分页结果
     */
    PageResult<CommentDTO> getPostComments(CommentQueryDTO queryDTO, Long userId);

    /**
     * 获取评论树
     * @param postId 文章ID
     * @param userId 用户ID
     * @return 评论树
     */
    List<CommentDTO> getCommentTree(Long postId, Long userId);

    /**
     * 获取用户评论列表
     * @param queryDTO 查询条件DTO
     * @param userId 用户ID
     * @return 评论分页结果
     */
    PageResult<CommentDTO> getUserComments(CommentQueryDTO queryDTO, Long userId);

    /**
     * 获取最新评论列表 (admin)
     * @param queryDTO 查询条件DTO
     * @return 评论分页结果
     */
    PageResult<CommentDTO> getLatestComments(CommentQueryDTO queryDTO);

    /**
     * 更新评论状态(admin/moderator)
     * @param dto 评论状态DTO
     */
    void updateCommentStatus(CommentStatusDTO dto);

    /**
     * 判断是否可以删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return true/false
     */
    boolean canDeleteComment(Long commentId, Long userId);

    /**
     * 判断是否可以更新评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return true/false
     */
    boolean canUpdateComment(Long commentId, Long userId);
    /**
     * 获取文章评论数量
     * @param postId 文章ID
     * @return 评论数量
     */
    Long getPostCommentCount(Long postId);

    /**
     * 获取用户评论数量
     * @param userId 用户ID
     * @return 评论数量
     */
    Long getUserCommentCount(Long userId);



}
