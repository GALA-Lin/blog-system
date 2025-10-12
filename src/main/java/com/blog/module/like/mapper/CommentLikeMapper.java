package com.blog.module.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.CommentLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-11-11:18
 * @Description:
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    /**
     * 检查用户是否已点赞评论
     */
    @Select("SELECT COUNT(*) > 0 FROM comment_likes WHERE comment_id = #{commentId} AND user_id = #{userId}")
    Boolean isLikedByUser(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 获取评论的点赞用户列表（分页）
     */
    IPage<CommentLike> selectCommentLikesWithUser(Page<?> page, @Param("commentId") Long commentId);

    /**
     * 获取用户的点赞评论列表（分页）
     */
    IPage<CommentLike> selectUserLikesWithComment(Page<?> page, @Param("userId") Long userId);

    /**
     * 批量检查用户是否点赞了多个评论
     */
    java.util.List<Long> selectLikedCommentIdsByUser(@Param("commentIds") java.util.List<Long> commentIds, @Param("userId") Long userId);

    /**
     * 删除点赞记录
     */
    @Delete("DELETE FROM comment_likes WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int deleteByCommentAndUser(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
