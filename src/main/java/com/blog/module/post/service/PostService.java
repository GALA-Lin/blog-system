package com.blog.module.post.service;

import com.blog.DTO.post.PostCreateDTO;
import com.blog.DTO.post.PostUpdateDTO;
import com.blog.VO.post.PostDetailVO;
import com.blog.VO.post.PostListVO;
import com.blog.common.PageResult;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:36
 * @Description:
 */
public interface PostService {
    /**
     * 创建文章
     * @param dto 文章创建DTO
     * @return 文章ID
     */
    Long createPost(PostCreateDTO dto);

    /**
     * 根据文章ID获取文章详情
     * @param postId 文章ID
     * @return 文章详情
     */
    PostDetailVO getPostById(Long postId);

    /**
     * 获取文章列表
     * @param page 页码
     * @param size 每页大小
     * @param status 文章状态
     * @return 文章列表
     */
    PageResult<PostListVO> getPostList(Integer page, Integer size, Integer status);

    /**
     * 更新文章
     * @param dto 文章更新DTO
     */
    void updatePost(PostUpdateDTO dto);

    /**
     * 删除文章
     * @param postId 文章ID
     */
    void deletePost(Long postId);

    /**
     * 发布文章
     * @param postId 文章ID
     */
    void publishPost(Long postId);

    /**
     * 浏览量自增
     * @param id 文章ID
     */
    void incrementViewCount(Long id);

}
