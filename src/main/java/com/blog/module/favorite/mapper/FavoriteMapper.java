package com.blog.module.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Favorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-11:40
 * @Description:
 */
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 检查用户是否已收藏文章
     * @param userId 用户ID
     * @param postId 文章ID
     * @return true：已收藏，false：未收藏
     */
    @Select("SELECT COUNT(*) > 0 FROM favorites WHERE user_id = #{userId} AND post_id = #{postId}")
    Boolean isPostFavorited(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 获取用户收藏的文章（带文章信息）
     * @param page 分页对象
     * @param userId 用户ID
     * @param folderId 收藏夹ID
     * @param sortBy 排序字段
     * @param sortOrder 排序方法
     * @return 分页对象
     */
    IPage<Favorite> selectUserFavoritesWithPost(
            Page<?> page,
            @Param("userId") Long userId,
            @Param("folderId") Long folderId,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 获取文件夹中的文字
     * @param page 分页对象
     * @param folderId 收藏夹ID
     * @return 分页对象
     */
    IPage<Favorite> selectFolderFavorites(
            Page<?> page,
            @Param("folderId") Long folderId
    );

    /**
     * 获取文章的收藏用户
     * @param page 分页对象
     * @param postId 文章ID
     * @return 分页对像
     */
    IPage<Favorite> selectPostFavoritesWithUser(
            Page<?> page,
            @Param("postId") Long postId
    );

    /**
     * 获取用户收藏的文章ID列表
     * @param userId 用户ID
     * @param postIds 文章ID列表
     * @return
     */
    @Select({
            "<script>",
            "SELECT post_id FROM favorites",
            "WHERE user_id = #{userId}",
            "AND post_id IN",
            "<foreach collection='postIds' item='postId' open='(' separator=',' close=')'>",
            "#{postId}",
            "</foreach>",
            "</script>"
    })
    List<Long> selectFavoritedPostIdsByUser(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    /**
     * 根据用户和文章查询收藏记录
     * @param userId 用户ID
     * @param postId 文章ID
     * @return 收藏记录
     */
    @Select("SELECT * FROM favorites WHERE user_id = #{userId} AND post_id = #{postId}")
    Favorite selectByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 删除收藏记录
     * @param userId 用户ID
     * @param postId 文章ID
     * @return 影响行数
     */
    @Delete("DELETE FROM favorites WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 批量移动收藏到新收藏夹
     * @param favoriteIds 收藏ID列表
     * @param targetFolderId 目标收藏夹ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update({
            "<script>",
            "UPDATE favorites SET folder_id = #{targetFolderId}",
            "WHERE id IN",
            "<foreach collection='favoriteIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "AND user_id = #{userId}",
            "</script>"
    })
    int batchMove(@Param("favoriteIds") List<Long> favoriteIds, @Param("targetFolderId") Long targetFolderId, @Param("userId") Long userId);

    /**
     * 统计用户收藏数
     * @param userId 用户ID
     * @return 收藏数
     */
    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计文章被收藏数
     * @param postId 文章ID
     * @return 收藏数
     */
    @Select("SELECT COUNT(*) FROM favorites WHERE post_id = #{postId}")
    Long countByPostId(@Param("postId") Long postId);
}
