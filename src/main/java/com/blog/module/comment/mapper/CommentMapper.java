package com.blog.module.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-10:09
 * @Description:
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    /**
     * 获取评论详情，包括作者信息
     * @param commentId 评论ID
     * @return 评论详情
     */
    @Select("SELECT c.*, " +
            "u.id as author_id, u.username as author_username, u.nickname as author_nickname, " +
            "u.avatar_url as author_avatar, " +
            "ru.id as reply_to_user_id, ru.username as reply_to_username, ru.nickname as reply_to_nickname " +
            "FROM comments c " +
            "INNER JOIN users u ON c.user_id = u.id " +
            "LEFT JOIN users ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.id = #{commentId} AND c.status != -1")
    Comment selectCommentWithAuthor(@Param("commentId") Long commentId);

    /**
     * 分页查询文章评论,仅顶级评论
     * @param page 分页对象
     * @param postId 文章ID
     * @param status 评论状态
     * @param currentUserId 当前登录用户ID
     * @return 分页对象
     */
    IPage<Comment> selectPostCommentsWithAuthor(
            Page<?> page,
            @Param("postId") Long postId,
            @Param("status") Integer status,
            @Param("currentUserId") Long currentUserId
    );

    /**
     * 根据评论ID查询评论的回复列表
     * @param commentId 评论ID
     * @param currentUserId 当前登录用户ID
     * @return 评论的回复列表
     */
    List<Comment> selectRepliesByCommentId(
            @Param("commentId") Long commentId,
            @Param("currentUserId") Long currentUserId
    );

    /**
     * 分页查询用户评论
     * @param page 分页对象
     * @param userId 用户ID
     * @param currentUserId 当前登录用户ID
     * @return 分页对象
     */
    IPage<Comment> selectUserComments(
            Page<?> page,
            @Param("userId") Long userId,
            @Param("currentUserId") Long currentUserId
    );

    /**
     * 根据文章ID查询文章评论数量
     * @param postId 文章ID
     * @return 文章评论数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE post_id = #{postId} AND status = 1")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 根据用户ID查询用户评论数量
     * @param userId 用户ID
     * @return 用户评论数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE user_id = #{userId} AND status = 1")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE comments SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE comments SET like_count = like_count - 1 WHERE id = #{commentId} AND like_count > 0")
    int decrementLikeCount(@Param("commentId") Long commentId);
    /**
     * 获取所有帖子的最新评论（用于管理员控制面板）
     * @param page 分页对象
     * @param status 评论状态
     * @return 分页对象
     */
    IPage<Comment> selectLatestComments(
            Page<?> page,
            @Param("status") Integer status
    );

    /**
     * 批量更新评论状态
     * @param ids 评论ID列表
     * @param status 评论状态
     * @return 影响行数
     */
    @Update("UPDATE comments SET status = #{status} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 根据文章ID查询评论树
     * @param postId 文章ID
     * @param currentUserId 当前登录用户ID
     * @return
     */
    List<Comment> selectCommentTree(@Param("postId") Long postId, @Param("currentUserId") Long currentUserId);

}

