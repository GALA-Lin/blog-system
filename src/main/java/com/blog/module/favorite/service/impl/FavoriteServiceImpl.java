package com.blog.module.favorite.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.DTO.favorite.*;
import com.blog.VO.auth.UserSimpleVO;
import com.blog.VO.favorite.FavoriteVO;
import com.blog.VO.favorite.FolderVO;
import com.blog.VO.post.PostSimpleVO;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.entity.Favorite;
import com.blog.entity.FavoriteFolder;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.module.favorite.mapper.FavoriteFolderMapper;
import com.blog.module.favorite.mapper.FavoriteMapper;
import com.blog.module.favorite.service.FavoriteService;
import com.blog.module.notification.service.NotificationService;
import com.blog.module.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-17:12
 * @Description:
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteServiceImpl implements  FavoriteService {

    private final FavoriteFolderMapper favoriteFolderMapper;
    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    // ========== 收藏夹管理 ==========

    /**
     * 转换收藏夹实体为 VO
     */
    private FolderVO convertFolderToVO(FavoriteFolder folder) {
        FolderVO vo = new FolderVO();
        BeanUtils.copyProperties(folder, vo);
        return vo;
    }

    /**
     * 转换收藏实体为 VO
     */
    private FavoriteVO convertFavoriteToVO(Favorite favorite) {
        FavoriteVO vo = new FavoriteVO();
        BeanUtils.copyProperties(favorite, vo);

        // 转换文章信息
        Post post = postMapper.selectById(favorite.getPostId());
        if (post != null) {
            PostSimpleVO postVO = new PostSimpleVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setViewCount(Math.toIntExact(post.getViewCount()));
            vo.setPost(postVO);
        }

        // 转换收藏夹信息
        if (favorite.getFolderId() != null) {
            FavoriteFolder folder = favoriteFolderMapper.selectById(favorite.getFolderId());
            if (folder != null) {
                FolderSimpleDTO folderDTO = new FolderSimpleDTO();
                BeanUtils.copyProperties(folder, folderDTO);
                vo.setFolder(folderDTO);
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FolderVO createFolder(FolderCreateDTO dto, Long userId) {
        // 检查名称是否重复
        if (Boolean.TRUE.equals(favoriteFolderMapper.existsByName(userId, dto.getName(), 0L))) {
            throw new BusinessException("收藏夹名称已存在");
        }

        FavoriteFolder folder = new FavoriteFolder();
        BeanUtils.copyProperties(dto, folder);
        folder.setUserId(userId);
        folder.setIsDefault(0);
        folder.setPostCount(0);

        favoriteFolderMapper.insert(folder);

        return convertFolderToVO(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FolderVO updateFolder(FolderUpdateDTO dto, Long userId) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(dto.getId());
        // 检查是否有权限修改
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }

        // 权限检查
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此收藏夹");
        }

        // 不能修改默认收藏夹的默认状态
        if (folder.getIsDefault() == 1) {
            throw new BusinessException("不能修改默认收藏夹");
        }

        // 检查名称是否重复
        if (!folder.getName().equals(dto.getName())) {
            if (Boolean.TRUE.equals(favoriteFolderMapper.existsByName(userId, dto.getName(), dto.getId()))) {
                throw new BusinessException("收藏夹名称已存在");
            }
        }
        BeanUtils.copyProperties(dto, folder);
        favoriteFolderMapper.updateById(folder);

        return convertFolderToVO(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId, Long userId) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }

        // 权限检查
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此收藏夹");
        }

        // 不能删除默认收藏夹
        if (folder.getIsDefault() == 1) {
            throw new BusinessException("不能删除默认收藏夹");
        }

        // 检查收藏夹是否为空
        if (folder.getPostCount() > 0) {
            throw new BusinessException("收藏夹不为空，请先移动或删除收藏");
        }

        favoriteFolderMapper.deleteById(folderId);
    }

    @Override
    public FolderVO getFolderDetail(Long folderId, Long userId) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }

        // 私密收藏夹只有所有者可以查看
        if (folder.getIsPublic() == 0 && !folder.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此收藏夹");
        }

        return convertFolderToVO(folder);
    }

    @Override
    public List<FolderVO> getUserFolders(Long userId) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        List<FavoriteFolder> folders = favoriteFolderMapper.selectUserFolders(userId);
        return folders.stream()
                .map(this::convertFolderToVO)
                .collect(Collectors.toList());
    }

    @Override
    public FolderVO getDefaultFolder(Long userId) {
        FavoriteFolder folder = favoriteFolderMapper.selectDefaultFolder(userId);

        // 如果不存在默认收藏夹，创建一个
        if (folder == null) {
            folder = new FavoriteFolder();
            folder.setUserId(userId);
            folder.setName("默认收藏夹");
            folder.setDescription("系统自动创建的默认收藏夹");
            folder.setIsPublic(0);
            folder.setIsDefault(1);
            folder.setPostCount(0);
            folder.setSortOrder(0);
            favoriteFolderMapper.insert(folder);
        }

        return convertFolderToVO(folder);
    }

    // ========== 收藏管理 ==========


    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteVO favoritePost(FavoriteCreateDTO dto, Long userId) {
        // 检查文章是否存在
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        if (post.getStatus() != 1) {
            throw new BusinessException("无法收藏未发布的文章");
        }

        // 检查是否已收藏
        if (Boolean.TRUE.equals(favoriteMapper.isPostFavorited(userId, dto.getPostId()))) {
            throw new BusinessException("已经收藏过此文章");
        }

        // 确定收藏夹
        Long folderId = dto.getFolderId();
        if (folderId == null) {
            // 使用默认收藏夹
            FolderVO defaultFolder = getDefaultFolder(userId);
            folderId = defaultFolder.getId();
        } else {
            // 检查收藏夹是否存在且属于当前用户
            FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
            if (folder == null || !folder.getUserId().equals(userId)) {
                throw new BusinessException("收藏夹不存在或无权访问");
            }
        }
        try {
            // 创建收藏记录
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setPostId(dto.getPostId());
            favorite.setFolderId(folderId);
            favorite.setNotes(dto.getNotes());
            favoriteMapper.insert(favorite);

            // 更新收藏夹文章数
            favoriteFolderMapper.updatePostCount(folderId, 1);

            // 更新文章收藏数
            postMapper.incrementFavoriteCount(dto.getPostId());
            // 发送通知
            if (!post.getUserId().equals(userId)) {
                try {
                    notificationService.sendFavoriteNotification(
                            post.getUserId(),    // 接收者：文章作者
                            userId,              // 发送者：收藏用户
                            dto.getPostId()      // 相关ID：文章ID
                    );
                } catch (Exception e) {
                    log.error("发送收藏通知失败", e);
                }
            }

            return convertFavoriteToVO(favorite);
        } catch (DuplicateKeyException e) {
            log.warn("重复收藏: postId={}, userId={}", dto.getPostId(), userId);
            throw new BusinessException("已经收藏过此文章");
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unfavoritePost(Long postId, Long userId) {
        Favorite favorite = favoriteMapper.selectByUserAndPost(userId, postId);

        if (favorite == null) {
            return false; // 未收藏过
        }

        // 删除收藏记录
        favoriteMapper.deleteById(favorite.getId());

        // 更新收藏夹文章数
        if (favorite.getFolderId() != null) {
            favoriteFolderMapper.updatePostCount(favorite.getFolderId(), -1);
        }

        // 更新文章收藏数
        postMapper.decrementFavoriteCount(postId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleFavorite(Long postId, Long userId, Long folderId) {
        if (Boolean.TRUE.equals(isPostFavorited(postId, userId))) {
            unfavoritePost(postId, userId);
            return false; // 取消收藏
        } else {
            FavoriteCreateDTO dto = new FavoriteCreateDTO();
            dto.setPostId(postId);
            dto.setFolderId(folderId);
            favoritePost(dto, userId);
            return true; // 收藏
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteVO updateFavorite(FavoriteUpdateDTO dto, Long userId) {
        Favorite favorite = favoriteMapper.selectById(dto.getId());
        if (favorite == null) {
            throw new BusinessException("收藏记录不存在");
        }

        // 权限检查
        if (!favorite.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此收藏");
        }

        Long oldFolderId = favorite.getFolderId();
        Long newFolderId = dto.getFolderId();
        boolean folderChanged = newFolderId != null && !newFolderId.equals(oldFolderId);

        // 如果移动收藏夹
        if (folderChanged) {
            // 检查新收藏夹是否存在且属于当前用户
            FavoriteFolder newFolder = favoriteFolderMapper.selectById(newFolderId);
            if (newFolder == null || !newFolder.getUserId().equals(userId)) {
                throw new BusinessException("新收藏夹不存在或无权访问");
            }

            // 更新旧收藏夹计数
            favoriteFolderMapper.updatePostCount(oldFolderId, -1);
            // 更新新收藏夹计数
            favoriteFolderMapper.updatePostCount(newFolderId, 1);
            favorite.setFolderId(newFolderId);
        }

        // 更新笔记
        if (dto.getNotes() != null) {
            favorite.setNotes(dto.getNotes());
        }

        favoriteMapper.updateById(favorite);
        return convertFavoriteToVO(favorite);
    }

    @Override
    public Boolean isPostFavorited(Long postId, Long userId) {
        if (postId == null || userId == null) {
            return false;
        }
        return favoriteMapper.isPostFavorited(userId, postId);
    }

    @Override
    public Map<Long, Boolean> batchCheckFavorites(List<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> favoritedPostIds = favoriteMapper.selectFavoritedPostIdsByUser(userId, postIds);

        Map<Long, Boolean> result = new HashMap<>();
        for (Long postId : postIds) {
            result.put(postId, favoritedPostIds.contains(postId));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMoveFavorites(FavoriteBatchMoveDTO dto, Long userId) {
        // 检查目标收藏夹
        FavoriteFolder targetFolder = favoriteFolderMapper.selectById(dto.getTargetFolderId());
        if (targetFolder == null || !targetFolder.getUserId().equals(userId)) {
            throw new BusinessException("目标收藏夹不存在或无权访问");
        }

        // 获取所有要移动的收藏记录
        List<Favorite> favorites = favoriteMapper   .selectBatchIds(dto.getFavoriteIds());

        // 统计各收藏夹的变化
        Map<Long, Integer> folderChanges = new HashMap<>();

        for (Favorite favorite : favorites) {
            // 权限检查
            if (!favorite.getUserId().equals(userId)) {
                continue;
            }

            Long oldFolderId = favorite.getFolderId();
            if (oldFolderId != null) {
                folderChanges.put(oldFolderId, folderChanges.getOrDefault(oldFolderId, 0) - 1);
            }

            folderChanges.put(dto.getTargetFolderId(),
                    folderChanges.getOrDefault(dto.getTargetFolderId(), 0) + 1);
        }

        // 批量移动
        favoriteMapper.batchMove(dto.getFavoriteIds(), dto.getTargetFolderId(), userId);

        // 更新各收藏夹计数
        for (Map.Entry<Long, Integer> entry : folderChanges.entrySet()) {
            favoriteFolderMapper.updatePostCount(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public PageResult<FavoriteVO> getUserFavorites(FavoriteQueryDTO queryDTO, Long userId) {
        Page<Favorite> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Favorite> favoritePage = favoriteMapper.selectUserFavoritesWithPost(
                page,
                queryDTO.getUserId() != null ? queryDTO.getUserId() : userId,
                queryDTO.getFolderId(),
                queryDTO.getSortBy(),
                queryDTO.getSortOrder()
        );

        List<FavoriteVO> voList = favoritePage.getRecords().stream()
                .map(this::convertFavoriteToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, favoritePage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public PageResult<FavoriteVO> getFolderFavorites(Long folderId, Integer pageNum,
                                                     Integer pageSize, Long userId) {
        // 检查收藏夹权限
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }

        // 私密收藏夹只有所有者可以查看
        if (folder.getIsPublic() == 0 && !folder.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此收藏夹");
        }

        Page<Favorite> page = new Page<>(pageNum, pageSize);
        IPage<Favorite> favoritePage = favoriteMapper.selectFolderFavorites(page, folderId);

        List<FavoriteVO> voList = favoritePage.getRecords().stream()
                .map(this::convertFavoriteToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, favoritePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<UserSimpleVO> getPostFavoriteUsers(Long postId, Integer pageNum, Integer pageSize) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        IPage<Favorite> favoritePage = favoriteMapper.selectPostFavoritesWithUser(page, postId);

        List<UserSimpleVO> users = favoritePage.getRecords().stream()
                .map(favorite -> {
                    UserSimpleVO vo = new UserSimpleVO();
                    User user = favorite.getUser();
                    if (user != null) {
                        vo.setId(user.getId());
                        vo.setUsername(user.getUsername());
                        vo.setNickname(user.getNickname());
                        vo.setAvatarUrl(user.getAvatarUrl());
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(users, favoritePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public Long getUserFavoriteCount(Long userId) {
        return favoriteMapper.countByUserId(userId);
    }

    @Override
    public Long getPostFavoriteCount(Long postId) {
        return favoriteMapper.countByPostId(postId);
    }
}
