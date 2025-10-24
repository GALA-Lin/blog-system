package com.blog.module.like.consumer;

import com.blog.DTO.mq.LikeMessage;
import com.blog.config.RabbitMQConfig;
import com.blog.entity.CommentLike;
import com.blog.entity.PostLike;
import com.blog.module.comment.mapper.CommentMapper;
import com.blog.module.like.mapper.CommentLikeMapper;
import com.blog.module.like.mapper.PostLikeMapper;
import com.blog.module.post.mapper.PostMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-23-22:51
 * @Description:
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeMessageConsumer {

    private final PostLikeMapper postLikeMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    /**
     * 消费文章点赞消息
     */
    @RabbitListener(queues = RabbitMQConfig.LIKE_POST_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handlePostLikeMessage(LikeMessage message, Message mqMessage, Channel channel) {
        try {
            log.info("【MQ消费】收到文章点赞消息: {}", message);

            if ("LIKE".equals(message.getAction())) {
                // 处理点赞
                handlePostLike(message);
            } else if ("UNLIKE".equals(message.getAction())) {
                // 处理取消点赞
                handlePostUnlike(message);
            }

            // 手动确认消息
            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
            log.info("【MQ消费】文章点赞消息处理成功: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("【MQ消费】处理文章点赞消息失败: {}", message, e);
            try {
                // 消息重新入队
                channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception ex) {
                log.error("【MQ消费】消息重新入队失败", ex);
            }
        }
    }

    /**
     * 消费评论点赞消息
     */
    @RabbitListener(queues = RabbitMQConfig.LIKE_COMMENT_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleCommentLikeMessage(LikeMessage message, Message mqMessage, Channel channel) {
        try {
            log.info("【MQ消费】收到评论点赞消息: {}", message);

            if ("LIKE".equals(message.getAction())) {
                handleCommentLike(message);
            } else if ("UNLIKE".equals(message.getAction())) {
                handleCommentUnlike(message);
            }

            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
            log.info("【MQ消费】评论点赞消息处理成功: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("【MQ消费】处理评论点赞消息失败: {}", message, e);
            try {
                channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception ex) {
                log.error("【MQ消费】消息重新入队失败", ex);
            }
        }
    }

    /**
     * 处理文章点赞
     */
    private void handlePostLike(LikeMessage message) {
        try {
            // 插入点赞记录
            PostLike postLike = new PostLike();
            postLike.setPostId(message.getTargetId());
            postLike.setUserId(message.getUserId());
            postLikeMapper.insert(postLike);

            // 增加文章点赞数
            postMapper.incrementLikeCount(message.getTargetId());

            log.info("【数据库更新】文章点赞成功: userId={}, postId={}",
                    message.getUserId(), message.getTargetId());

        } catch (DuplicateKeyException e) {
            // 重复点赞，忽略（幂等性保证）
            log.warn("【数据库更新】重复点赞记录: userId={}, postId={}",
                    message.getUserId(), message.getTargetId());
        }
    }

    /**
     * 处理文章取消点赞
     */
    private void handlePostUnlike(LikeMessage message) {
        // 删除点赞记录
        int deleted = postLikeMapper.deleteByPostAndUser(message.getTargetId(), message.getUserId());

        if (deleted > 0) {
            // 减少文章点赞数
            postMapper.decrementLikeCount(message.getTargetId());

            log.info("【数据库更新】取消文章点赞成功: userId={}, postId={}",
                    message.getUserId(), message.getTargetId());
        } else {
            log.warn("【数据库更新】点赞记录不存在: userId={}, postId={}",
                    message.getUserId(), message.getTargetId());
        }
    }

    /**
     * 处理评论点赞
     */
    private void handleCommentLike(LikeMessage message) {
        try {
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(message.getTargetId());
            commentLike.setUserId(message.getUserId());
            commentLikeMapper.insert(commentLike);

            // 增加评论点赞数
            commentMapper.incrementLikeCount(message.getTargetId());

            log.info("【数据库更新】评论点赞成功: userId={}, commentId={}",
                    message.getUserId(), message.getTargetId());

        } catch (DuplicateKeyException e) {
            log.warn("【数据库更新】重复点赞记录: userId={}, commentId={}",
                    message.getUserId(), message.getTargetId());
        }
    }

    /**
     * 处理评论取消点赞
     */
    private void handleCommentUnlike(LikeMessage message) {
        int deleted = commentLikeMapper.deleteByCommentAndUser(
                message.getTargetId(), message.getUserId());

        if (deleted > 0) {
            commentMapper.decrementLikeCount(message.getTargetId());

            log.info("【数据库更新】取消评论点赞成功: userId={}, commentId={}",
                    message.getUserId(), message.getTargetId());
        } else {
            log.warn("【数据库更新】点赞记录不存在: userId={}, commentId={}",
                    message.getUserId(), message.getTargetId());
        }
    }
}
