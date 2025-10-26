package com.blog.scheduler;

import com.blog.DTO.mq.CountSyncMessage;
import com.blog.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-25-13:27
 * @Description:
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 同步点赞计数到数据库
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncLikeCount() {
        log.info("【定时任务】开始同步点赞计数到数据库");

        try {
            // 1. 同步文章点赞数
            syncPostLikeCount();

            // 2. 同步评论点赞数
            syncCommentLikeCount();

            log.info("【定时任务】点赞计数同步完成");

        } catch (Exception e) {
            log.error("【定时任务】点赞计数同步失败", e);
        }
    }

    /**
     * 同步文章点赞数
     */
    private void syncPostLikeCount() {
        String pattern = "post:*:like_count";

        // 使用SCAN命令遍历所有匹配的key
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(ScanOptions.scanOptions().match(pattern).count(100).build())) {

            int count = 0;
            while (cursor.hasNext()) {
                String key = new String(cursor.next());

                // 提取文章ID
                Long postId = extractIdFromKey(key);
                if (postId == null) {
                    continue;
                }

                // 获取Redis中的点赞数
                Object countObj = redisTemplate.opsForValue().get(key);
                if (countObj == null) {
                    continue;
                }

                Integer likeCount = Integer.parseInt(countObj.toString());

                // 发送MQ消息进行同步
                CountSyncMessage message = CountSyncMessage.builder()
                        .messageId(UUID.randomUUID().toString())
                        .type("LIKE_POST")
                        .targetId(postId)
                        .delta(likeCount)
                        .timestamp(LocalDateTime.now())
                        .build();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.LIKE_EXCHANGE,
                        RabbitMQConfig.LIKE_SYNC_ROUTING_KEY,
                        message
                );

                count++;
            }

            log.info("【定时任务】文章点赞数同步: {} 条", count);

        } catch (Exception e) {
            log.error("【定时任务】文章点赞数同步失败", e);
        }
    }

    /**
     * 同步评论点赞数
     */
    private void syncCommentLikeCount() {
        String pattern = "comment:*:like_count";

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(ScanOptions.scanOptions().match(pattern).count(100).build())) {

            int count = 0;
            while (cursor.hasNext()) {
                String key = new String(cursor.next());

                Long commentId = extractIdFromKey(key);
                if (commentId == null) {
                    continue;
                }

                Object countObj = redisTemplate.opsForValue().get(key);
                if (countObj == null) {
                    continue;
                }

                Integer likeCount = Integer.parseInt(countObj.toString());

                CountSyncMessage message = CountSyncMessage.builder()
                        .messageId(UUID.randomUUID().toString())
                        .type("LIKE_COMMENT")
                        .targetId(commentId)
                        .delta(likeCount)
                        .timestamp(LocalDateTime.now())
                        .build();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.LIKE_EXCHANGE,
                        RabbitMQConfig.LIKE_SYNC_ROUTING_KEY,
                        message
                );

                count++;
            }

            log.info("【定时任务】评论点赞数同步: {} 条", count);

        } catch (Exception e) {
            log.error("【定时任务】评论点赞数同步失败", e);
        }
    }

    /**
     * 清理过期的Redis缓存
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredCache() {
        log.info("【定时任务】开始清理过期Redis缓存");

        try {
            // 清理用户点赞缓存（超过7天未访问）
            cleanUserLikeCache();

            log.info("【定时任务】过期缓存清理完成");

        } catch (Exception e) {
            log.error("【定时任务】过期缓存清理失败", e);
        }
    }

    /**
     * 清理用户点赞缓存
     */
    private void cleanUserLikeCache() {
        // 这里可以根据业务需求实现具体的清理逻辑
        log.info("【定时任务】用户点赞缓存清理");
    }

    /**
     * 从Redis Key中提取ID
     * 例如: "post:123:like_count" -> 123
     */
    private Long extractIdFromKey(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (Exception e) {
            log.error("【定时任务】解析Key失败: {}", key, e);
        }
        return null;
    }

}
