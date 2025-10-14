package com.blog.module.favorite.controller;

import com.blog.DTO.favorite.*;
import com.blog.VO.auth.UserSimpleVO;
import com.blog.VO.favorite.FavoriteVO;
import com.blog.VO.favorite.FolderVO;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.module.favorite.service.FavoriteService;
import com.blog.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-20:31
 * @Description:
 */
@Tag(name = "收藏管理", description = "收藏夹和收藏相关API")
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ========== 收藏夹管理 ==========

    @Operation(summary = "创建收藏夹")
    @PostMapping("/folders")
    @PreAuthorize("isAuthenticated()")
    public Result<FolderVO> createFolder(@Valid @RequestBody FolderCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderVO folder = favoriteService.createFolder(dto, userId);
        return Result.success("收藏夹创建成功", folder);
    }

    @Operation(summary = "更新收藏夹")
    @PutMapping("/folders/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<FolderVO> updateFolder(
            @PathVariable Long id,
            @Valid @RequestBody FolderUpdateDTO dto) {
        dto.setId(id);
        Long userId = SecurityUtil.getCurrentUserId();
        FolderVO folder = favoriteService.updateFolder(dto, userId);
        return Result.success("收藏夹更新成功", folder);
    }

    @Operation(summary = "删除收藏夹")
    @DeleteMapping("/folders/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteFolder(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        favoriteService.deleteFolder(id, userId);
        return Result.success("收藏夹删除成功");
    }

    @Operation(summary = "获取收藏夹详情")
    @GetMapping("/folders/{id}")
    public Result<FolderVO> getFolderDetail(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserIdOrNull();
        FolderVO folder = favoriteService.getFolderDetail(id, userId);
        return Result.success(folder);
    }

    @Operation(summary = "获取我的收藏夹列表")
    @GetMapping("/folders/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<FolderVO>> getMyFolders() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<FolderVO> folders = favoriteService.getUserFolders(userId);
        return Result.success(folders);
    }

    @Operation(summary = "获取用户的收藏夹列表")
    @GetMapping("/folders/user/{userId}")
    public Result<List<FolderVO>> getUserFolders(@PathVariable Long userId) {
        List<FolderVO> folders = favoriteService.getUserFolders(userId);
        return Result.success(folders);
    }

    @Operation(summary = "获取默认收藏夹")
    @GetMapping("/folders/default")
    @PreAuthorize("isAuthenticated()")
    public Result<FolderVO> getDefaultFolder() {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderVO folder = favoriteService.getDefaultFolder(userId);
        return Result.success(folder);
    }
    // ========== 收藏管理 ==========

    @Operation(summary = "收藏文章")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<FavoriteVO> favoritePost(@Valid @RequestBody FavoriteCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        FavoriteVO favorite = favoriteService.favoritePost(dto, userId);
        return Result.success("收藏成功", favorite);
    }

    @Operation(summary = "取消收藏文章")
    @DeleteMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> unfavoritePost(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean result = favoriteService.unfavoritePost(postId, userId);
        return Result.success(result ? "取消收藏成功" : "您还没有收藏过此文章", result);
    }

    @Operation(summary = "切换收藏状态", description = "已收藏则取消，未收藏则收藏到默认收藏夹")
    @PutMapping("/post/{postId}/toggle")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> toggleFavorite(
            @PathVariable Long postId,
            @RequestParam(required = false) Long folderId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean isFavorited = favoriteService.toggleFavorite(postId, userId, folderId);
        return Result.success(isFavorited ? "收藏成功" : "取消收藏成功", isFavorited);
    }

    @Operation(summary = "更新收藏（移动收藏夹或修改笔记）")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<FavoriteVO> updateFavorite(
            @PathVariable Long id,
            @Valid @RequestBody FavoriteUpdateDTO dto) {
        dto.setId(id);
        Long userId = SecurityUtil.getCurrentUserId();
        FavoriteVO favorite = favoriteService.updateFavorite(dto, userId);
        return Result.success("更新成功", favorite);
    }

    @Operation(summary = "检查文章是否已收藏")
    @GetMapping("/post/{postId}/check")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> checkFavorite(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean isFavorited = favoriteService.isPostFavorited(postId, userId);
        return Result.success(isFavorited);
    }

    @Operation(summary = "批量检查文章收藏状态")
    @PostMapping("/posts/batch-check")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<Long, Boolean>> batchCheckFavorites(@RequestBody List<Long> postIds) {
        Long userId = SecurityUtil.getCurrentUserId();
        Map<Long, Boolean> result = favoriteService.batchCheckFavorites(postIds, userId);
        return Result.success(result);
    }

    @Operation(summary = "批量移动收藏到新收藏夹")
    @PutMapping("/batch-move")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> batchMoveFavorites(@Valid @RequestBody FavoriteBatchMoveDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        favoriteService.batchMoveFavorites(dto, userId);
        return Result.success("批量移动成功");
    }

    // ========== 收藏查询 ==========

    @Operation(summary = "获取我的收藏列表")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<FavoriteVO>> getMyFavorites(@Valid FavoriteQueryDTO queryDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        queryDTO.setUserId(userId);
        PageResult<FavoriteVO> favorites = favoriteService.getUserFavorites(queryDTO, userId);
        return Result.success(favorites);
    }

    @Operation(summary = "获取用户的收藏列表")
    @GetMapping("/user/{userId}")
    public Result<PageResult<FavoriteVO>> getUserFavorites(
            @PathVariable Long userId,
            @Valid FavoriteQueryDTO queryDTO) {
        queryDTO.setUserId(userId);
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        PageResult<FavoriteVO> favorites = favoriteService.getUserFavorites(queryDTO, currentUserId);
        return Result.success(favorites);
    }

    @Operation(summary = "获取收藏夹中的文章列表")
    @GetMapping("/folders/{folderId}/posts")
    public Result<PageResult<FavoriteVO>> getFolderFavorites(
            @PathVariable Long folderId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserIdOrNull();
        PageResult<FavoriteVO> favorites = favoriteService.getFolderFavorites(
                folderId, pageNum, pageSize, userId);
        return Result.success(favorites);
    }

    @Operation(summary = "获取文章的收藏用户列表")
    @GetMapping("/post/{postId}/users")
    public Result<PageResult<UserSimpleVO>> getPostFavoriteUsers(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<UserSimpleVO> users = favoriteService.getPostFavoriteUsers(
                postId, pageNum, pageSize);
        return Result.success(users);
    }

    @Operation(summary = "获取我的收藏总数")
    @GetMapping("/my/count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getMyFavoriteCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        Long count = favoriteService.getUserFavoriteCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "获取文章被收藏总数")
    @GetMapping("/post/{postId}/count")
    public Result<Long> getPostFavoriteCount(@PathVariable Long postId) {
        Long count = favoriteService.getPostFavoriteCount(postId);
        return Result.success(count);
    }

}
