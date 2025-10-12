package com.blog.module.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.PostLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-11-02:03
 * @Description:
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    /**
     * 判断用户是否对文章点过赞
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true：已点赞，false：未点赞
     */
    @Select("SELECT COUNT(*) > 0 FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    Boolean isLikedByUser(@Param("postId") Long postId, @Param("userId") Long userId);


    /**
     * 获取文章的点赞用户列表
     * @param page 分页对象
     * @param postId 文章ID
     * @return 文章的点赞用户列表
     */
    IPage<PostLike> selectPostLikesWithUser(Page<?> page, @Param("postId") Long postId);

    /**
     * 获取用户的文章点赞列表
     * @param page 分页对象
     * @param userId 用户ID
     * @return 用户的文章点赞列表
     */
    IPage<PostLike> selectUserLikesWithPost(Page<?> page, @Param("userId") Long userId);

    /**
     * 批量获取用户是否对文章点过赞
     * @param postIds 文章ID列表
     * @param userId 用户ID
     * @return 用户是否对文章点过赞的列表
     */
    java.util.List<Long> selectLikedPostIdsByUser(@Param("postIds") java.util.List<Long> postIds, @Param("userId") Long userId);

    /**
     * 删除用户对文章的点赞
     * @param postId 文章ID
     * @param userId 用户ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}
