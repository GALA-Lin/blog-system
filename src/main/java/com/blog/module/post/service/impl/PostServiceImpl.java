package com.blog.module.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.DTO.post.PostCreateDTO;
import com.blog.DTO.post.PostUpdateDTO;
import com.blog.VO.post.CategoryVO;
import com.blog.VO.post.PostDetailVO;
import com.blog.VO.post.PostListVO;
import com.blog.VO.post.TagVO;
import com.blog.entity.*;

import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.common.ResultCode;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.module.post.mapper.*;
import com.blog.module.post.service.PostService;
import com.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:42
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostCategoryMapper postCategoryMapper;
    private final PostTagMapper postTagMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    /**
     * 创建文章
     * @param dto 文章创建DTO
     * @return id 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostCreateDTO dto) {
        // 校验用户是否登录
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setSummary(dto.getSummary());
        post.setContent(dto.getContent());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus());
        post.setContentType("MARKDOWN");
        post.setSlug(generateSlug(dto.getTitle()));// 从标题生成 slug（URL 友好型字符串 / URL 别名）

        // 设置发布时间
        if (dto.getStatus() == 1){
            post.setPublishedAt(LocalDateTime.now());
        }
        // 处理分类与标签
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            saveCategoriesForPost(post.getId(), dto.getCategoryIds());
        }

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveTagsForPost(post.getId(), dto.getTagIds());
        }

        postMapper.insert(post);
        return post.getId();
    }
    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .substring(0, Math.min(title.length(), 50));
    }
    private void saveCategoriesForPost(Long postId, List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            PostCategory postCategory = new PostCategory();
            postCategory.setPostId(postId);
            postCategory.setCategoryId(categoryId);
            postCategoryMapper.insert(postCategory);
        }
    }

    // 辅助方法：保存文章标签关联
    private void saveTagsForPost(Long postId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            postTagMapper.insert(postTag);
        }
    }
    /**
     * 获取文章详情
     * @param id 文章ID
     * @return 文章详情VO
     */
    @Override
    public PostDetailVO getPostById(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        // 加载文章信息
        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);
        // 加载作者信息
        User author = userMapper.selectById(post.getUserId());
        if (author != null){
            vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            vo.setAuthorAvatar(author.getAvatarUrl());
        }
        // 加载分类
        List<CategoryVO> categories = loadCategoriesForPost(id);
        vo.setCategories(categories);
        // 加载标签
        List<TagVO> tags = loadTagsForPost(id);
        vo.setTags(tags);
        return vo;
    }

    private List<CategoryVO> loadCategoriesForPost(Long postId) {

        LambdaQueryWrapper<PostCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostCategory::getPostId, postId);
        List<PostCategory> postCategories = postCategoryMapper.selectList(wrapper);

        List<CategoryVO> categoryVOs = new ArrayList<>();
        for (PostCategory pc : postCategories) {
            Category category = categoryMapper.selectById(pc.getCategoryId());
            if (category != null) {
                CategoryVO vo = new CategoryVO();
                vo.setId(category.getId());
                vo.setName(category.getName());
                vo.setSlug(category.getSlug());
                categoryVOs.add(vo);
            }
        }
        return categoryVOs;
    }
    private List<TagVO> loadTagsForPost(Long postId) {
        LambdaQueryWrapper<PostTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostTag::getPostId, postId);
        List<PostTag> postTags = postTagMapper.selectList(wrapper);

        List<TagVO> tagVOs = new ArrayList<>();
        for (PostTag pt : postTags) {
            Tag tag = tagMapper.selectById(pt.getTagId());
            if (tag != null) {
                TagVO vo = new TagVO();
                vo.setId(tag.getId());
                vo.setName(tag.getName());
                vo.setSlug(tag.getSlug());
                tagVOs.add(vo);
            }
        }
        return tagVOs;
    }


    /**
     * 获取文章列表
     * @param page 页码
     * @param size 每页大小
     * @param status 文章状态
     * @return 文章列表分页
     */
    @Override
    public PageResult<PostListVO> getPostList(Integer page, Integer size, Integer status) {
        // 创建查询条件
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        // 如果有状态参数，则筛选状态
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        // 按照发布时间倒序排序
        wrapper.orderByDesc(Post::getPublishedAt);
        // 分页查询文章列表
        Page<Post> pageParam = new Page<>(page, size);
        Page<Post> pageResult = postMapper.selectPage(pageParam, wrapper);

        // 转换成 VO 并返回
        List<PostListVO> voList = pageResult.getRecords().stream()
                .map(post -> {
                    PostListVO vo = new PostListVO();
                    // 复制基础属性
                    BeanUtils.copyProperties(post, vo);
                    // 加载并设置作者信息
                    User author = userMapper.selectById(post.getUserId());
                    if (author != null) {
                        vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
                        vo.setAuthorAvatar(author.getAvatarUrl());
                    }
                    return vo;
                })
                .toList();

        return PageResult.of(pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent(), voList);
    }

    /**
     * 更新文章
     * @param dto 文章更新DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(PostUpdateDTO dto) {
        // 校验文章是否存在
        Post post = postMapper.selectById(dto.getId());
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        // 校验用户是否有权限修改
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!post.getUserId().equals(currentUserId) &&
                !SecurityUtil.hasRole("ROLE_ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 更新文章
        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
            post.setSlug(generateSlug(dto.getTitle()));
        }
        if (dto.getSummary() != null) {
            post.setSummary(dto.getSummary());
        }
        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }
        if (dto.getCoverImage() != null) {
            post.setCoverImage(dto.getCoverImage());
        }
        if (dto.getStatus() != null) {
            post.setStatus(dto.getStatus());
        }
        postMapper.updateById(post);
    }

    /**
     * 删除文章
     * @param id 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        // 检查文章是否存在
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 校验用户是否有权限删除
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!post.getUserId().equals(currentUserId) &&
                !SecurityUtil.hasRole("ROLE_ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 软删除文章：将状态设置为 -1
        post.setStatus(-1);
        postMapper.updateById(post);
    }

    /**
     * 发布文章
     * @param id 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPost(Long id) {
        Post post = postMapper.selectById(id);
        // 校验文章是否存在
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        // 校验用户是否有权限发布
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 发布文章：将状态设置为 1，设置发布时间
        post.setStatus(1);
        post.setPublishedAt(LocalDateTime.now());
        postMapper.updateById(post);


    }
    /**
     * 增加文章阅读量
     * @param id 文章ID
     */
    @Override
    public void incrementViewCount(Long id) {
        postMapper.incrementViewCount(id);
    }

}
