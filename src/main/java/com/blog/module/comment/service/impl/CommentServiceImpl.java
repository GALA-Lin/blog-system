package com.blog.module.comment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.VO.auth.UserSimpleDTO;
import com.blog.DTO.comment.*;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;

import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.module.comment.mapper.CommentMapper;
import com.blog.module.comment.service.ICommentService;
import com.blog.module.notification.service.INotificationService;
import com.blog.module.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final INotificationService notificationService;

    /**
     * 创建评论
     * @param dto 评论创建DTO
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param userAgent 用户代理，识别用户
     * @return 评论详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDTO createComment(CommentCreateDTO dto, Long userId, String ipAddress, String userAgent) {
        // 验证文章存在且允许评论
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        if (post.getStatus() != 1) {
            throw new BusinessException("文章未发布，无法评论");
        }
        if (post.getAllowComment() == 0) {
            throw new BusinessException("评论已关闭");
        }

        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setIpAddress(ipAddress);
        comment.setUserAgent(userAgent);
        comment.setStatus(1); // 自动通过审核
        comment.setLikeCount(0);
        comment.setReplyCount(0);

        // 处理父评论并回复
        if (dto.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(dto.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
            if (!parentComment.getPostId().equals(dto.getPostId())) {
                throw new BusinessException("父评论不属于当前文章");
            }

            comment.setParentId(dto.getParentId());
            comment.setRootId(parentComment.getRootId() != null ? parentComment.getRootId() : parentComment.getId());

            if (dto.getReplyToUserId() != null) {
                comment.setReplyToUserId(dto.getReplyToUserId());
            } else {
                comment.setReplyToUserId(parentComment.getUserId());
            }
        }

        // 插入评论
        commentMapper.insert(comment);

        // fix：取消触发器，更新父评论的 reply_count
        if (comment.getParentId() != null) {
            updateParentReplyCount(comment.getRootId());
        }


        // 发送通知
        // TODO: 临时service,异步发送通知
        try {
            // 通知作者
            if (!post.getUserId().equals(userId)) {
                notificationService.createCommentNotification(post.getUserId(), userId, comment.getId(), post.getId());
            }

            // 通知被回复者
            if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(userId)) {
                notificationService.createReplyNotification(comment.getReplyToUserId(), userId, comment.getId(), post.getId());
            }
        } catch (Exception e) {
            log.error("未能发送通知", e);
        }

        return getCommentDetail(comment.getId(), userId);
    }
    // 更新父评论的 reply_count
    private void updateParentReplyCount(Long parentId) {
        try {
            // 计算父评论的实际回复数
            Long replyCount = commentMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Comment>()
                            .eq(Comment::getParentId, parentId)
                            .eq(Comment::getStatus, 1)
            );

            // 更新父评论
            Comment parent = new Comment();
            parent.setId(parentId);
            parent.setReplyCount(replyCount.intValue());
            commentMapper.updateById(parent);

            log.debug("更新父评论 {} 的 reply_count 为 {}", parentId, replyCount);
        } catch (Exception e) {
            log.error("更新父评论的 reply_count 失败", e);
        }
    }

    /**
     * 更新评论
     * @param dto 评论更新DTO
     * @param userId 用户ID
     * @return 评论详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDTO updateComment(CommentUpdateDTO dto, Long userId) {
        Comment comment = commentMapper.selectById(dto.getId());
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能更新自己的评论");
        }

        if (comment.getStatus() == -1) {
            throw new BusinessException("评论已删除，无法更新");
        }

        comment.setContent(dto.getContent());
        commentMapper.updateById(comment);

        return getCommentDetail(comment.getId(), userId);
    }

    /**
     * 删除评论(软删除)
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!canDeleteComment(commentId, userId)) {
            throw new BusinessException("你没有权限删除此评论");
        }

        comment.setStatus(-1);
        commentMapper.updateById(comment);

    }

    /**
     * 获取评论详情
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 评论详情
     */
    @Override
    public CommentDTO getCommentDetail(Long commentId, Long userId) {
        Comment comment = commentMapper.selectCommentWithAuthor(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        CommentDTO dto = convertToDTO(comment);

        // 加载回复
        if (comment.getParentId() == null) {
            List<Comment> replies = commentMapper.selectRepliesByCommentId(commentId, userId);
            dto.setChildren(replies.stream().map(this::convertToDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * 获取文章评论列表
     * @param queryDTO 查询条件DTO
     * @param userId 用户ID
     * @return 文章评论列表
     */
    @Override
    public PageResult<CommentDTO> getPostComments(CommentQueryDTO queryDTO, Long userId) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Comment> commentPage = commentMapper.selectPostCommentsWithAuthor(
                page,
                queryDTO.getPostId(),
                queryDTO.getStatus() != null ? queryDTO.getStatus() : 1,
                userId
        );

        List<CommentDTO> dtoList = commentPage.getRecords().stream()
                .map(comment -> {
                    CommentDTO dto = convertToDTO(comment);
                    // Load replies if requested
                    if (Boolean.TRUE.equals(queryDTO.getLoadReplies())) {
                        List<Comment> replies = commentMapper.selectRepliesByCommentId(comment.getId(), userId);
                        dto.setChildren(replies.stream().map(this::convertToDTO).collect(Collectors.toList()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, commentPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 获取评论树
     * @param postId 文章ID
     * @param userId 用户ID
     * @return 根评论
     */
    @Override
    public List<CommentDTO> getCommentTree(Long postId, Long userId) {
        List<Comment> allComments = commentMapper.selectCommentTree(postId, userId);

        // 建立评论ID与DTO的映射
        Map<Long, CommentDTO> commentMap = new HashMap<>();
        List<CommentDTO> rootComments = new ArrayList<>();

        // 转化为DTO
        for (Comment comment : allComments) {
            CommentDTO dto = convertToDTO(comment);
            dto.setChildren(new ArrayList<>());
            commentMap.put(dto.getId(), dto);
        }

        // 建立评论树
        for (CommentDTO dto : commentMap.values()) {
            if (dto.getParentId() == null) {
                rootComments.add(dto);
            } else {
                CommentDTO parent = commentMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootComments;
    }

    /**
     * 获取用户的评论列表
     * @param queryDTO 查询条件DTO
     * @param userId 用户ID
     * @return 用户评论列表
     */
    @Override
    public PageResult<CommentDTO> getUserComments(CommentQueryDTO queryDTO, Long userId) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Comment> commentPage = commentMapper.selectUserComments(page, queryDTO.getUserId(), userId);

        List<CommentDTO> dtoList = commentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, commentPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 获取最新评论列表
     * @param queryDTO 查询条件DTO
     * @return 最新评论列表
     */
    @Override
    public PageResult<CommentDTO> getLatestComments(CommentQueryDTO queryDTO) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        IPage<Comment> commentPage = commentMapper.selectLatestComments(page, queryDTO.getStatus());

        List<CommentDTO> dtoList = commentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, commentPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 批量更新评论状态
     * @param dto 评论状态DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentStatus(CommentStatusDTO dto) {
        commentMapper.batchUpdateStatus(dto.getIds(), dto.getStatus());

        log.info("批量更新评论状态，共 {} 条，状态为 {}，原因为 {}",
                dto.getIds().size(), dto.getStatus(), dto.getReason());
    }

    /**
     * 判断用户是否有权限删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否有权限删除评论
     */
    @Override
    public boolean canDeleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // 用户只能删除自己的评论
        return comment.getUserId().equals(userId);

        //管理员删除任何评论
    }

    /**
     * 判断用户是否有权限更新评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否有权限更新评论
     */
    @Override
    public boolean canUpdateComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // Only author can update
        return comment.getUserId().equals(userId);
    }

    /**
     * 获取文章评论数量
     * @param postId 文章ID
     * @return 文章评论数量
     */
    @Override
    public Long getPostCommentCount(Long postId) {
        return commentMapper.countByPostId(postId);
    }

    /**
     * 获取用户评论数量
     * @param userId 用户ID
     * @return 用户评论数量
     */
    @Override
    public Long getUserCommentCount(Long userId) {
        return commentMapper.countByUserId(userId);
    }


    /**
     * 转换评论为DTO
     * @param comment 评论
     * @return 评论DTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // 作者
        if (comment.getAuthor() != null) {
            UserSimpleDTO author = new UserSimpleDTO();
            BeanUtils.copyProperties(comment.getAuthor(), author);
            dto.setAuthor(author);
        }

        // 回复者
        if (comment.getReplyToUser() != null) {
            UserSimpleDTO replyToUser = new UserSimpleDTO();
            BeanUtils.copyProperties(comment.getReplyToUser(), replyToUser);
            dto.setReplyToUser(replyToUser);
        }

        return dto;
    }
}