package com.blog.module.favorite.service;

import com.blog.DTO.favorite.*;
import com.blog.VO.auth.UserSimpleVO;
import com.blog.VO.favorite.FavoriteVO;
import com.blog.VO.favorite.FolderVO;
import com.blog.common.PageResult;

import java.util.List;
import java.util.Map;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-17:01
 * @Description:
 */
public interface FavoriteService {
    // ========== 收藏夹管理 ==========

    /**
     * 创建收藏夹
     * @param dto 收藏夹创建DTO
     * @param userId 用户ID
     * @return 收藏夹信息
     */
    FolderVO createFolder(FolderCreateDTO dto, Long userId);

    /**
     * 更新收藏夹
     * @param dto 收藏夹更新DTO
     * @param userId 用户ID
     * @return 更新后的收藏夹信息
     */
    FolderVO updateFolder(FolderUpdateDTO dto, Long userId);

    /**
     * 删除收藏夹
     * @param folderId 收藏夹ID
     * @param userId 用户ID
     */
    void deleteFolder(Long folderId, Long userId);

    /**
     * 获取收藏夹详情
     * @param folderId 收藏夹ID
     * @param userId 用户ID
     * @return 收藏夹详情
     */
    FolderVO getFolderDetail(Long folderId, Long userId);

    /**
     * 获取用户收藏夹列表
     * @param userId 用户ID
     * @return 用户收藏夹列表
     */
    List<FolderVO>getUserFolders(Long userId);

    /**
     * 获取用户的默认收藏夹
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    FolderVO getDefaultFolder(Long userId);

    // ========== 收藏管理 ==========

    /**
     * 收藏文章
     * @param dto 收藏DTO
     * @param userId 用户ID
     * @return 收藏信息
     */
    FavoriteVO favoritePost(FavoriteCreateDTO dto, Long userId);

    /**
     * 取消收藏文章
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true=取消成功, false=未收藏过
     */
    Boolean unfavoritePost(Long postId, Long userId);

    /**
     * 切换收藏状态
     * @param postId 文章ID
     * @param userId 用户ID
     * @param folderId 收藏夹ID
     * @return true=收藏成功, false=取消收藏
     */
    Boolean toggleFavorite(Long postId, Long userId, Long folderId);

    /**
     * 更新收藏（移动收藏夹或修改笔记）
     * @param dto 更新DTO
     * @param userId 用户ID
     * @return 更新后的收藏信息
     */
    FavoriteVO updateFavorite(FavoriteUpdateDTO dto, Long userId);

    /**
     * 检查文章是否已收藏
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true=已收藏, false=未收藏
     */
    Boolean isPostFavorited(Long postId, Long userId);

    /**
     * 批量检查文章收藏状态
     * @param postIds 文章ID列表
     * @param userId 用户ID
     * @return Map<文章ID, 是否收藏>
     */
    Map<Long, Boolean> batchCheckFavorites(List<Long> postIds, Long userId);

    /**
     * 批量移动收藏到新收藏夹
     * @param dto 批量移动DTO
     * @param userId 用户ID
     */
    void batchMoveFavorites(FavoriteBatchMoveDTO dto, Long userId);

    // ========== 收藏查询 ==========

    /**
     * 获取用户的收藏列表
     * @param queryDTO 查询条件
     * @param userId 用户ID
     * @return 分页收藏列表
     */
    PageResult<FavoriteVO> getUserFavorites(FavoriteQueryDTO queryDTO, Long userId);

    /**
     * 获取收藏夹中的文章列表
     * @param folderId 收藏夹ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 当前用户ID（用于权限检查）
     * @return 分页收藏列表
     */
    PageResult<FavoriteVO> getFolderFavorites(Long folderId, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 获取文章的收藏用户列表
     * @param postId 文章ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页用户列表
     */
    PageResult<UserSimpleVO> getPostFavoriteUsers(Long postId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户收藏总数
     * @param userId 用户ID
     * @return 收藏总数
     */
    Long getUserFavoriteCount(Long userId);

    /**
     * 获取文章被收藏总数
     * @param postId 文章ID
     * @return 被收藏总数
     */
    Long getPostFavoriteCount(Long postId);
}
