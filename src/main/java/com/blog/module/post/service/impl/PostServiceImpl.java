package com.blog.module.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.DTO.post.PostCreateDTO;
import com.blog.DTO.post.PostUpdateDTO;
import com.blog.VO.post.PostDetailVO;
import com.blog.VO.post.PostListVO;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.common.ResultCode;
import com.blog.entity.Post;
import com.blog.module.post.mapper.PostMapper;
import com.blog.module.post.service.PostService;
import com.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:42
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

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
        // 4. TODO: Handle categories and tags (Phase 2)

        postMapper.insert(post);
        return post.getId();
    }
    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .substring(0, Math.min(title.length(), 50));
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
        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);
        // 3. TODO: Load author info, categories, tags
        return vo;
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
                    BeanUtils.copyProperties(post, vo);
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
