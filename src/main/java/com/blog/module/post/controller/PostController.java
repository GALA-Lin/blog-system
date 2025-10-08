package com.blog.module.post.controller;

import com.blog.DTO.post.PostCreateDTO;
import com.blog.DTO.post.PostUpdateDTO;
import com.blog.VO.post.PostDetailVO;
import com.blog.VO.post.PostListVO;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.module.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-15:21
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "文章模块", description = "文章相关接口")
public class PostController {

    private final PostService postService;

    /**
     * 创建文章
     *
     * @param dto 文章创建DTO
     */
    @PostMapping
    @PreAuthorize("hasAuthority('post:create')")
    @Operation(summary = "创建文章", description = "创建文章")
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO dto) {
        Long postId = postService.createPost(dto);
        return Result.success("文章创建成功", postId);
    }

    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情", description = "获取文章详情")
    public Result<PostDetailVO> getPost(@PathVariable Long id) {
        PostDetailVO post = postService.getPostById(id);
        // 增加文章阅读量
        postService.incrementViewCount(id);
        return Result.success(post);
    }

    /**
     * 获取文章列表
     *
     * @param page   页码
     * @param size   每页大小
     * @param status 文章状态
     * @return 文章列表
     */
    @GetMapping
    @Operation(summary = "获取文章列表", description = "获取文章列表")
    public Result<PageResult<PostListVO>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        PageResult<PostListVO> result = postService.getPostList(page, size, status);
        return Result.success(result);
    }

    /**
     * 更新文章
     * @param id 文章ID
     * @param dto 文章更新DTO
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新文章")
    public Result<Void> updatePost(@PathVariable Long id,
                                   @Valid @RequestBody PostUpdateDTO dto) {
        dto.setId(id);
        postService.updatePost(dto);
        return Result.success("文章更新成功");
    }

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除文章")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success("文章删除成功");
    }

    /**
     * 发布文章
     *
     * @param id 文章ID
     * @return 发布结果
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('post:publish')")
    @Operation(summary = "发布文章")
    public Result<Void> publishPost(@PathVariable Long id) {
        postService.publishPost(id);
        return Result.success("文章已发布");
    }
}
