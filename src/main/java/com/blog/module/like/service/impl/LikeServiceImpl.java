package com.blog.module.like.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.DTO.mq.*;
import com.blog.VO.auth.UserSimpleVO;
import com.blog.VO.post.PostVO;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.config.RabbitMQConfig;
import com.blog.constants.SystemConstants;
import com.blog.entity.*;
import com.blog.module.comment.mapper.CommentMapper;
import com.blog.module.like.mapper.CommentLikeMapper;
import com.blog.module.like.mapper.PostLikeMapper;
import com.blog.module.like.service.LikeService;
import com.blog.module.notification.service.NotificationService;
import com.blog.module.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-11-13:00
 * @Description:
 *
 * 核心思路：
 * 1. 单次查询：Redis + DB 双查（以 Redis 为准，DB 为兜底）
 * 2. 批量查询：一次性加载所有点赞，然后标记预热
 * 3. 点赞/取消：更新 Redis（无需标记预热）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final PostLikeMapper postLikeMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    // Redis 缓存标记
    private static final String CACHE_INIT_FLAG = "like:cache:init:user:%d:%s"; // user:1:POST

    // ========== 文章点赞实现 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        if (post.getStatus() != 1) {
            throw new BusinessException("文章未发布");
        }

        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_POSTS, userId);
        String postLikeCountKey = String.format(SystemConstants.KEY_POST_LIKE_COUNT, postId);

        // 检查 Redis 中是否已点赞
        Boolean redisMember = redisTemplate.opsForSet().isMember(userLikeKey, postId);
        if (Boolean.TRUE.equals(redisMember)) {
            log.info("【Redis检测】用户{}已点赞文章{}", userId, postId);
            return false;
        }

        // ✅ 关键修复：即使 Redis 返回 false，也要检查数据库（防止 Redis 未预热）
        Boolean dbLiked = postLikeMapper.isLikedByUser(postId, userId);
        if (Boolean.TRUE.equals(dbLiked)) {
            // 数据库已点赞，同步到 Redis
            redisTemplate.opsForSet().add(userLikeKey, postId);
            redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);
            log.warn("【数据不一致】Redis未预热，已从DB同步：用户{}文章{}", userId, postId);
            return false;
        }

        // Redis 和 DB 都未点赞，执行点赞逻辑
        log.info("【Redis更新】用户{}点赞文章{}", userId, postId);
        redisTemplate.opsForSet().add(userLikeKey, postId);
        redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);

        redisTemplate.opsForValue().increment(postLikeCountKey, 1);
        redisTemplate.expire(postLikeCountKey, 7, TimeUnit.DAYS);

        // 发送 MQ 消息
        LikeMessage likeMessage = LikeMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(userId)
                .targetId(postId)
                .targetType("POST")
                .action("LIKE")
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LIKE_EXCHANGE,
                RabbitMQConfig.LIKE_POST_ROUTING_KEY,
                likeMessage
        );

        // 发送通知
        if (!post.getUserId().equals(userId)) {
            notificationService.sendLikeNotification(
                    post.getUserId(), userId, postId, "LIKE"
            );
        }

        return true;
    }

    @Override
    public Boolean unlikePost(Long postId, Long userId) {
        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_POSTS, userId);
        String postLikeCountKey = String.format(SystemConstants.KEY_POST_LIKE_COUNT, postId);

        // 检查 Redis
        Boolean redisMember = redisTemplate.opsForSet().isMember(userLikeKey, postId);

        // ✅ Redis 和 DB 双查
        Boolean dbLiked = postLikeMapper.isLikedByUser(postId, userId);

        if (Boolean.FALSE.equals(redisMember) && Boolean.FALSE.equals(dbLiked)) {
            log.info("【检测】用户{}未点赞文章{}", userId, postId);
            return false;
        }

        // 更新 Redis
        log.info("【Redis更新】用户{}取消点赞文章{}", userId, postId);
        redisTemplate.opsForSet().remove(userLikeKey, postId);
        redisTemplate.opsForValue().decrement(postLikeCountKey, 1);

        // 发送 MQ 消息
        LikeMessage likeMessage = LikeMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(userId)
                .targetId(postId)
                .targetType("POST")
                .action("UNLIKE")
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LIKE_EXCHANGE,
                RabbitMQConfig.LIKE_POST_ROUTING_KEY,
                likeMessage
        );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean togglePostLike(Long postId, Long userId) {
        if (Boolean.TRUE.equals(isPostLiked(postId, userId))) {
            return !unlikePost(postId, userId);
        } else {
            return likePost(postId, userId);
        }
    }

    @Override
    public Boolean isPostLiked(Long postId, Long userId) {
        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_POSTS, userId);

        // 检查缓存是否已预热
        if (isCacheWarmed(userId, "POST")) {
            // 缓存已预热，直接从 Redis 查询
            Boolean isMember = redisTemplate.opsForSet().isMember(userLikeKey, postId);
            log.debug("【Redis查询】用户{}点赞文章{}状态: {}（已预热）", userId, postId, isMember);
            return Boolean.TRUE.equals(isMember);
        }

        // 缓存未预热，Redis + DB 双查
        Boolean redisMember = redisTemplate.opsForSet().isMember(userLikeKey, postId);
        if (Boolean.TRUE.equals(redisMember)) {
            // Redis 有记录，直接返回
            log.debug("【Redis命中】用户{}点赞文章{}（未预热但命中）", userId, postId);
            return true;
        }

        // 数据库兜底
        Boolean dbLiked = postLikeMapper.isLikedByUser(postId, userId);
        if (Boolean.TRUE.equals(dbLiked)) {
            // 数据库有记录，同步到 Redis
            redisTemplate.opsForSet().add(userLikeKey, postId);
            redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);
            log.debug("【DB兜底】用户{}点赞文章{}，已同步Redis", userId, postId);
            return true;
        }

        log.debug("【未点赞】用户{}未点赞文章{}", userId, postId);
        return false;
    }

    @Override
    public Map<Long, Boolean> batchCheckPostLikes(List<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }

        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_POSTS, userId);
        Map<Long, Boolean> result = new HashMap<>(postIds.size());

        // 检查缓存是否已预热
        if (isCacheWarmed(userId, "POST")) {
            // 已预热，直接从 Redis 批量查询
            for (Long postId : postIds) {
                Boolean isMember = redisTemplate.opsForSet().isMember(userLikeKey, postId);
                result.put(postId, Boolean.TRUE.equals(isMember));
            }
            log.debug("【Redis批量查询】用户{}的点赞状态（已预热）", userId);
            return result;
        }

        // 预热：从 DB 一次性加载所有点赞记录
        log.info("【缓存预热】开始预热用户{}的文章点赞缓存", userId);
        List<Long> allLikedPostIds = postLikeMapper.selectLikedPostIdsByUser(postIds, userId);

        // 批量写入 Redis
        if (!allLikedPostIds.isEmpty()) {
            redisTemplate.opsForSet().add(userLikeKey, allLikedPostIds.toArray());
            redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);
        }

        // 标记缓存已预热（关键：只有批量加载后才标记）
        markCacheWarmed(userId, "POST");

        // 构建返回结果
        Set<Long> likedSet = new HashSet<>(allLikedPostIds);
        for (Long postId : postIds) {
            result.put(postId, likedSet.contains(postId));
        }

        log.info("【缓存预热完成】用户{}的文章点赞缓存，共{}条", userId, allLikedPostIds.size());
        return result;
    }

    /**
     * 检查缓存是否已预热
     * @param userId 用户ID
     * @param type 类型：POST / COMMENT
     * @return true=已预热，false=未预热
     */
    private boolean isCacheWarmed(Long userId, String type) {
        String flagKey = String.format(CACHE_INIT_FLAG, userId, type);
        return redisTemplate.hasKey(flagKey);
    }

    /**
     * 标记缓存已预热（仅在批量加载后调用）
     * @param userId 用户ID
     * @param type 类型：POST / COMMENT
     */
    private void markCacheWarmed(Long userId, String type) {
        String flagKey = String.format(CACHE_INIT_FLAG, userId, type);
        redisTemplate.opsForValue().set(flagKey, "1", 7, TimeUnit.DAYS);
        log.info("【缓存标记】用户{}的{}点赞缓存已预热", userId, type);
    }

    @Override
    public PageResult<UserSimpleVO> getPostLikeUsers(Long postId, Integer pageNum, Integer pageSize) {
        Page<PostLike> page = new Page<>(pageNum, pageSize);
        IPage<PostLike> likePage = postLikeMapper.selectPostLikesWithUser(page, postId);

        List<UserSimpleVO> users = likePage.getRecords().stream()
                .map(like -> {
                    UserSimpleVO dto = new UserSimpleVO();
                    User user = like.getUser();
                    if (user != null) {
                        dto.setId(user.getId());
                        dto.setUsername(user.getUsername());
                        dto.setNickname(user.getNickname());
                        dto.setAvatarUrl(user.getAvatarUrl());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(users, likePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<PostVO> getUserLikePosts(Long userId, Integer pageNum, Integer pageSize) {
        Page<PostLike> page = new Page<>(pageNum, pageSize);
        IPage<PostLike> likePage = postLikeMapper.selectUserLikesWithPost(page, userId);

        List<PostVO> posts = likePage.getRecords().stream()
                .map(like -> {
                    PostVO dto = new PostVO();
                    Post post = like.getPost();
                    if (post != null) {
                        dto.setId(post.getId());
                        dto.setTitle(post.getTitle());
                        dto.setSlug(post.getSlug());
                        dto.setSummary(post.getSummary());
                        dto.setContent(post.getContent());
                        dto.setContentType(post.getContentType());
                        dto.setCoverImage(post.getCoverImage());
                        dto.setStatus(post.getStatus());
                        dto.setIsTop(post.getIsTop());
                        dto.setIsOriginal(post.getIsOriginal());
                        dto.setOriginalUrl(post.getOriginalUrl());
                        dto.setViewCount(Math.toIntExact(post.getViewCount()));
                        dto.setLikeCount(post.getLikeCount());
                        dto.setFavoriteCount(post.getFavoriteCount());
                        dto.setCommentCount(post.getCommentCount());
                        dto.setAllowComment(post.getAllowComment());
                        dto.setPublishedAt(post.getPublishedAt());
                        dto.setCreatedAt(post.getCreatedAt());
                        dto.setUpdatedAt(post.getUpdatedAt());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(posts, likePage.getTotal(), pageNum, pageSize);
    }

    // ========== 评论点赞 ==========

    @Override
    public Boolean likeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectCommentWithAuthor(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (comment.getStatus() != 1) {
            throw new BusinessException("评论不可用");
        }

        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_COMMENTS, userId);
        String commentLikeCountKey = String.format(SystemConstants.KEY_COMMENT_LIKE_COUNT, commentId);

        // Redis + DB 双查
        Boolean redisMember = redisTemplate.opsForSet().isMember(userLikeKey, commentId);
        if (Boolean.TRUE.equals(redisMember)) {
            return false;
        }

        Boolean dbLiked = commentLikeMapper.isLikedByUser(commentId, userId);
        if (Boolean.TRUE.equals(dbLiked)) {
            redisTemplate.opsForSet().add(userLikeKey, commentId);
            redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);
            log.warn("【数据不一致】Redis未预热，已从DB同步：用户{}评论{}", userId, commentId);
            return false;
        }

        // 执行点赞
        log.info("【Redis更新】用户{}点赞评论{}", userId, commentId);
        redisTemplate.opsForSet().add(userLikeKey, commentId);
        redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);

        redisTemplate.opsForValue().increment(commentLikeCountKey, 1);
        redisTemplate.expire(commentLikeCountKey, 7, TimeUnit.DAYS);

        // 发送 MQ
        LikeMessage likeMessage = LikeMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(userId)
                .targetId(commentId)
                .targetType("COMMENT")
                .action("LIKE")
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LIKE_EXCHANGE,
                RabbitMQConfig.LIKE_COMMENT_ROUTING_KEY,
                likeMessage
        );

        // 发送通知
        if (!comment.getUserId().equals(userId)) {
            notificationService.sendLikeNotification(
                    comment.getUserId(), userId, commentId, "LIKE_COMMENT"
            );
        }

        return true;
    }

    @Override
    public Boolean unlikeComment(Long commentId, Long userId) {
        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_COMMENTS, userId);
        String commentLikeCountKey = String.format(SystemConstants.KEY_COMMENT_LIKE_COUNT, commentId);

        // Redis + DB 双查
        Boolean redisMember = redisTemplate.opsForSet().isMember(userLikeKey, commentId);
        Boolean dbLiked = commentLikeMapper.isLikedByUser(commentId, userId);

        if (Boolean.FALSE.equals(redisMember) && Boolean.FALSE.equals(dbLiked)) {
            return false;
        }

        // 更新 Redis
        log.info("【Redis更新】用户{}取消点赞评论{}", userId, commentId);
        redisTemplate.opsForSet().remove(userLikeKey, commentId);
        redisTemplate.opsForValue().decrement(commentLikeCountKey, 1);

        // 发送 MQ
        LikeMessage likeMessage = LikeMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(userId)
                .targetId(commentId)
                .targetType("COMMENT")
                .action("UNLIKE")
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LIKE_EXCHANGE,
                RabbitMQConfig.LIKE_COMMENT_ROUTING_KEY,
                likeMessage
        );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleCommentLike(Long commentId, Long userId) {
        if (Boolean.TRUE.equals(isCommentLiked(commentId, userId))) {
            return !unlikeComment(commentId, userId);
        } else {
            return likeComment(commentId, userId);
        }
    }

    @Override
    public Boolean isCommentLiked(Long commentId, Long userId) {
        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_COMMENTS, userId);

        // 已预热：直接查 Redis
        if (isCacheWarmed(userId, "COMMENT")) {
            Boolean isMember = redisTemplate.opsForSet().isMember(userLikeKey, commentId);
            return Boolean.TRUE.equals(isMember);
        }

        // 未预热：Redis + DB 双查
        Boolean redisMember = redisTemplate.opsForSet().isMember(userLikeKey, commentId);
        if (Boolean.TRUE.equals(redisMember)) {
            return true;
        }

        Boolean dbLiked = commentLikeMapper.isLikedByUser(commentId, userId);
        if (Boolean.TRUE.equals(dbLiked)) {
            redisTemplate.opsForSet().add(userLikeKey, commentId);
            redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);
            return true;
        }

        return false;
    }

    @Override
    public Map<Long, Boolean> batchCheckCommentLikes(List<Long> commentIds, Long userId) {
        if (commentIds == null || commentIds.isEmpty()) {
            return new HashMap<>();
        }

        String userLikeKey = String.format(SystemConstants.KEY_USER_LIKED_COMMENTS, userId);
        Map<Long, Boolean> result = new HashMap<>(commentIds.size());

        // 已预热：直接查 Redis
        if (isCacheWarmed(userId, "COMMENT")) {
            for (Long commentId : commentIds) {
                Boolean isMember = redisTemplate.opsForSet().isMember(userLikeKey, commentId);
                result.put(commentId, Boolean.TRUE.equals(isMember));
            }
            return result;
        }

        // 未预热：从 DB 批量加载
        log.info("【缓存预热】开始预热用户{}的评论点赞缓存", userId);
        List<Long> allLikedCommentIds = commentLikeMapper.selectLikedCommentIdsByUser(commentIds, userId);

        if (!allLikedCommentIds.isEmpty()) {
            redisTemplate.opsForSet().add(userLikeKey, allLikedCommentIds.toArray());
            redisTemplate.expire(userLikeKey, 7, TimeUnit.DAYS);
        }

        // 标记已预热
        markCacheWarmed(userId, "COMMENT");

        // 构建结果
        Set<Long> likedSet = new HashSet<>(allLikedCommentIds);
        for (Long commentId : commentIds) {
            result.put(commentId, likedSet.contains(commentId));
        }

        log.info("【缓存预热完成】用户{}的评论点赞缓存，共{}条", userId, allLikedCommentIds.size());
        return result;
    }

    @Override
    public PageResult<UserSimpleVO> getCommentLikeUsers(Long commentId, Integer pageNum, Integer pageSize) {
        Page<CommentLike> page = new Page<>(pageNum, pageSize);
        IPage<CommentLike> likePage = commentLikeMapper.selectCommentLikesWithUser(page, commentId);

        List<UserSimpleVO> users = likePage.getRecords().stream()
                .map(like -> {
                    UserSimpleVO dto = new UserSimpleVO();
                    User user = like.getUser();
                    if (user != null) {
                        dto.setId(user.getId());
                        dto.setUsername(user.getUsername());
                        dto.setNickname(user.getNickname());
                        dto.setAvatarUrl(user.getAvatarUrl());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(users, likePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<Long> getUserLikeComments(Long userId, Integer pageNum, Integer pageSize) {
        Page<CommentLike> page = new Page<>(pageNum, pageSize);
        IPage<CommentLike> likePage = commentLikeMapper.selectUserLikesWithComment(page, userId);

        List<Long> commentIds = likePage.getRecords().stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toList());

        return new PageResult<>(commentIds, likePage.getTotal(), pageNum, pageSize);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 检查缓存是否已初始化（预热）
     * @param userId 用户ID
     * @param type 类型：POST / COMMENT
     * @return true=已预热，false=未预热
     */
    private boolean isCacheInitialized(Long userId, String type) {
        String flagKey = String.format(CACHE_INIT_FLAG + ":%s", userId, type);
        return redisTemplate.hasKey(flagKey);
    }

    /**
     * 标记缓存已初始化
     * @param userId 用户ID
     * @param type 类型：POST / COMMENT
     */
    private void markCacheInitialized(Long userId, String type) {
        String flagKey = String.format(CACHE_INIT_FLAG + ":%s", userId, type);
        redisTemplate.opsForValue().set(flagKey, 1, 7, TimeUnit.DAYS);
    }
}