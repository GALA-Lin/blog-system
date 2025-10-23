package com.blog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-23-10:22
 * @Description:
 */
@Slf4j
@Configuration
public class RabbitMQConfig {
    // ==================== 交换机名称 ====================
    public static final String LIKE_EXCHANGE = "blog.like.exchange";
    public static final String FAVORITE_EXCHANGE = "blog.favorite.exchange";
    public static final String NOTIFICATION_EXCHANGE = "blog.notification.exchange";
    public static final String COMMENT_EXCHANGE = "blog.comment.exchange";

    // ==================== 队列名称 ====================
    // 点赞相关队列
    public static final String LIKE_POST_QUEUE = "blog.like.post.queue";
    public static final String LIKE_COMMENT_QUEUE = "blog.like.comment.queue";
    public static final String LIKE_SYNC_QUEUE = "blog.like.sync.queue";

    // 收藏相关队列
    public static final String FAVORITE_POST_QUEUE = "blog.favorite.post.queue";
    public static final String FAVORITE_SYNC_QUEUE = "blog.favorite.sync.queue";

    // 通知相关队列
    public static final String NOTIFICATION_QUEUE = "blog.notification.queue";

    // 评论相关队列
    public static final String COMMENT_COUNT_QUEUE = "blog.comment.count.queue";

    // ==================== 路由键 ====================
    public static final String LIKE_POST_ROUTING_KEY = "like.post";
    public static final String LIKE_COMMENT_ROUTING_KEY = "like.comment";
    public static final String LIKE_SYNC_ROUTING_KEY = "like.sync";

    public static final String FAVORITE_POST_ROUTING_KEY = "favorite.post";
    public static final String FAVORITE_SYNC_ROUTING_KEY = "favorite.sync";

    public static final String NOTIFICATION_ROUTING_KEY = "notification.*";

    public static final String COMMENT_COUNT_ROUTING_KEY = "comment.count";

    /**
     * 消息转换器 - 使用JSON格式
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        // 消息发送确认
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送成功: {}", correlationData);
            } else {
                log.error("消息发送失败: {}, cause: {}", correlationData, cause);
            }
        });

        // 消息返回确认
        template.setReturnsCallback(returned -> {
            log.error("消息未被路由: message={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });

        return template;
    }
    /**
     * 监听器容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        return factory;
    }


    // ==================== 点赞模块 ====================

    /**
     * 点赞交换机
     */
    @Bean
    public TopicExchange likeExchange() {
        return ExchangeBuilder.topicExchange(LIKE_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 文章点赞队列
     */
    @Bean
    public Queue likePostQueue() {
        return QueueBuilder.durable(LIKE_POST_QUEUE)
                .build();
    }

    /**
     * 评论点赞队列
     */
    @Bean
    public Queue likeCommentQueue() {
        return QueueBuilder.durable(LIKE_COMMENT_QUEUE)
                .build();
    }

    /**
     * 点赞同步队列（定时批量同步到数据库）
     */
    @Bean
    public Queue likeSyncQueue() {
        return QueueBuilder.durable(LIKE_SYNC_QUEUE)
                .build();
    }

    @Bean
    public Binding likePostBinding() {
        return BindingBuilder.bind(likePostQueue())
                .to(likeExchange())
                .with(LIKE_POST_ROUTING_KEY);
    }

    @Bean
    public Binding likeCommentBinding() {
        return BindingBuilder.bind(likeCommentQueue())
                .to(likeExchange())
                .with(LIKE_COMMENT_ROUTING_KEY);
    }

    @Bean
    public Binding likeSyncBinding() {
        return BindingBuilder.bind(likeSyncQueue())
                .to(likeExchange())
                .with(LIKE_SYNC_ROUTING_KEY);
    }

    // ==================== 收藏模块 ====================

    /**
     * 收藏交换机
     */
    @Bean
    public TopicExchange favoriteExchange() {
        return ExchangeBuilder.topicExchange(FAVORITE_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 文章收藏队列
     */
    @Bean
    public Queue favoritePostQueue() {
        return QueueBuilder.durable(FAVORITE_POST_QUEUE)
                .build();
    }

    /**
     * 收藏同步队列
     */
    @Bean
    public Queue favoriteSyncQueue() {
        return QueueBuilder.durable(FAVORITE_SYNC_QUEUE)
                .build();
    }

    @Bean
    public Binding favoritePostBinding() {
        return BindingBuilder.bind(favoritePostQueue())
                .to(favoriteExchange())
                .with(FAVORITE_POST_ROUTING_KEY);
    }

    @Bean
    public Binding favoriteSyncBinding() {
        return BindingBuilder.bind(favoriteSyncQueue())
                .to(favoriteExchange())
                .with(FAVORITE_SYNC_ROUTING_KEY);
    }

    // ==================== 通知模块 ====================

    /**
     * 通知交换机
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // ==================== 评论模块 ====================

    /**
     * 评论交换机
     */
    @Bean
    public TopicExchange commentExchange() {
        return ExchangeBuilder.topicExchange(COMMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 评论计数队列
     */
    @Bean
    public Queue commentCountQueue() {
        return QueueBuilder.durable(COMMENT_COUNT_QUEUE)
                .build();
    }

    @Bean
    public Binding commentCountBinding() {
        return BindingBuilder.bind(commentCountQueue())
                .to(commentExchange())
                .with(COMMENT_COUNT_ROUTING_KEY);
    }
}
