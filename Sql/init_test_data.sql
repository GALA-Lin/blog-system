-- ============================================
-- 测试数据初始化脚本
-- 用于快速创建测试环境
-- ============================================

-- 1. 清理旧数据（可选，谨慎使用）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
TRUNCATE TABLE posts;
TRUNCATE TABLE comments;
TRUNCATE TABLE categories;
TRUNCATE TABLE tags;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 2. 插入测试用户
-- ============================================
-- 密码：Test123456! (BCrypt 加密后)
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `avatar_url`, `bio`, `status`, `email_verified`) VALUES
('admin', 'admin@blog.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '系统管理员', 'https://i.pravatar.cc/150?img=1', '系统管理员账号', 1, 1),
('author1', 'author1@blog.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '作者一号', 'https://i.pravatar.cc/150?img=2', '热爱写作的技术博主', 1, 1),
('author2', 'author2@blog.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '作者二号', 'https://i.pravatar.cc/150?img=3', '全栈开发工程师', 1, 1),
('reader1', 'reader1@blog.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '读者小明', 'https://i.pravatar.cc/150?img=4', '技术爱好者', 1, 1),
('reader2', 'reader2@blog.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '读者小红', 'https://i.pravatar.cc/150?img=5', '前端开发者', 1, 1);

-- ============================================
-- 3. 分配角色
-- ============================================
-- admin 用户分配 ADMIN 角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(1, 1); -- admin -> ROLE_ADMIN

-- author1, author2 分配 AUTHOR 角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(2, 2), -- author1 -> ROLE_AUTHOR
(3, 2); -- author2 -> ROLE_AUTHOR

-- reader1, reader2 分配 READER 角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(4, 4), -- reader1 -> ROLE_READER
(5, 4); -- reader2 -> ROLE_READER

-- ============================================
-- 4. 插入分类数据（如果 SQL 脚本中没有）
-- ============================================
INSERT INTO `categories` (`name`, `slug`, `description`, `icon`, `sort_order`, `status`) VALUES
('后端开发', 'backend', 'Java、Spring Boot、数据库等后端技术', '🔧', 1, 1),
('前端开发', 'frontend', 'Vue、React、JavaScript 等前端技术', '🎨', 2, 1),
('DevOps', 'devops', 'Docker、K8s、CI/CD 等运维技术', '🚀', 3, 1),
('算法与数据结构', 'algorithm', '算法、数据结构、LeetCode 题解', '📊', 4, 1),
('架构设计', 'architecture', '系统架构、设计模式、微服务', '🏛️', 5, 1),
('数据库', 'database', 'MySQL、Redis、MongoDB 等数据库技术', '💾', 6, 1);

-- ============================================
-- 5. 插入标签数据
-- ============================================
INSERT INTO `tags` (`name`, `slug`, `color`) VALUES
('Java', 'java', '#e76f51'),
('Spring Boot', 'spring-boot', '#f4a261'),
('MyBatis', 'mybatis', '#e9c46a'),
('MySQL', 'mysql', '#2a9d8f'),
('Redis', 'redis', '#264653'),
('Vue', 'vue', '#41b883'),
('React', 'react', '#61dafb'),
('Docker', 'docker', '#0db7ed'),
('Kubernetes', 'kubernetes', '#326ce5'),
('算法', 'algorithm', '#e76f51'),
('LeetCode', 'leetcode', '#ffa116'),
('设计模式', 'design-pattern', '#8e44ad'),
('微服务', 'microservices', '#3498db'),
('分布式', 'distributed', '#e74c3c'),
('性能优化', 'performance', '#f39c12');

-- ============================================
-- 6. 插入测试文章
-- ============================================
INSERT INTO `posts` (`user_id`, `title`, `slug`, `summary`, `content`, `content_type`, `cover_image`, `status`, `is_original`, `view_count`, `like_count`, `published_at`) VALUES
(2, 'Spring Boot 3.0 新特性详解', 'spring-boot-3-features', 'Spring Boot 3.0 带来了许多重要更新，本文详细介绍其新特性', '# Spring Boot 3.0 新特性\n\n## 1. Java 17 基线\n\nSpring Boot 3.0 要求最低 Java 17...\n\n## 2. Jakarta EE 9\n\n迁移到 Jakarta EE 9，包名从 javax.* 变更为 jakarta.*', 'MARKDOWN', 'https://picsum.photos/800/400?random=1', 1, 1, 150, 25, NOW()),

(2, 'MyBatis-Plus 实战教程', 'mybatis-plus-tutorial', '深入浅出讲解 MyBatis-Plus 的核心功能和最佳实践', '# MyBatis-Plus 实战\n\n## 简介\n\nMyBatis-Plus 是 MyBatis 的增强工具...\n\n## 核心功能\n\n- CRUD 自动化\n- 条件构造器\n- 分页插件\n- 代码生成器', 'MARKDOWN', 'https://picsum.photos/800/400?random=2', 1, 1, 200, 38, NOW()),

(3, 'Vue 3 Composition API 最佳实践', 'vue3-composition-api', 'Vue 3 Composition API 让代码组织更灵活，本文分享实战经验', '# Vue 3 Composition API\n\n## setup() 函数\n\n```javascript\nimport { ref, computed } from "vue";\n\nexport default {\n  setup() {\n    const count = ref(0);\n    return { count };\n  }\n}\n```', 'MARKDOWN', 'https://picsum.photos/800/400?random=3', 1, 1, 180, 42, NOW()),

(3, 'Docker 容器化部署实践', 'docker-deployment', '使用 Docker 实现应用的容器化部署，提高开发效率', '# Docker 容器化部署\n\n## Dockerfile 编写\n\n```dockerfile\nFROM openjdk:17-alpine\nWORKDIR /app\nCOPY target/*.jar app.jar\nEXPOSE 8080\nENTRYPOINT ["java", "-jar", "app.jar"]\n```', 'MARKDOWN', 'https://picsum.photos/800/400?random=4', 1, 1, 120, 18, NOW()),

(2, 'Redis 缓存设计模式', 'redis-cache-patterns', '深入讲解 Redis 在实际项目中的缓存设计模式', '# Redis 缓存设计\n\n## Cache-Aside 模式\n\n读操作：先查缓存，未命中则查数据库并写入缓存。\n\n写操作：先更新数据库，再删除缓存。', 'MARKDOWN', 'https://picsum.photos/800/400?random=5', 1, 1, 220, 55, NOW()),

(3, 'LeetCode 热题 HOT 100 解析', 'leetcode-hot100', '精选 LeetCode 经典算法题目详细解析', '# LeetCode HOT 100\n\n## 两数之和\n\n```java\npublic int[] twoSum(int[] nums, int target) {\n    Map<Integer, Integer> map = new HashMap<>();\n    for (int i = 0; i < nums.length; i++) {\n        int complement = target - nums[i];\n        if (map.containsKey(complement)) {\n            return new int[] { map.get(complement), i };\n        }\n        map.put(nums[i], i);\n    }\n    throw new IllegalArgumentException("No solution");\n}\n```', 'MARKDOWN', 'https://picsum.photos/800/400?random=6', 1, 1, 300, 78, NOW());

-- ============================================
-- 7. 文章-分类关联
-- ============================================
INSERT INTO `post_categories` (`post_id`, `category_id`) VALUES
(1, 1), -- Spring Boot 3.0 -> 后端开发
(2, 1), -- MyBatis-Plus -> 后端开发
(3, 2), -- Vue 3 -> 前端开发
(4, 3), -- Docker -> DevOps
(5, 6), -- Redis -> 数据库
(6, 4); -- LeetCode -> 算法与数据结构

-- ============================================
-- 8. 文章-标签关联
-- ============================================
INSERT INTO `post_tags` (`post_id`, `tag_id`) VALUES
(1, 2), -- Spring Boot 3.0 -> Spring Boot
(1, 1), -- Spring Boot 3.0 -> Java
(2, 3), -- MyBatis-Plus -> MyBatis
(2, 1), -- MyBatis-Plus -> Java
(3, 6), -- Vue 3 -> Vue
(4, 8), -- Docker -> Docker
(5, 5), -- Redis -> Redis
(5, 14), -- Redis -> 分布式
(6, 10), -- LeetCode -> 算法
(6, 11); -- LeetCode -> LeetCode

-- 更新分类和标签的文章计数
UPDATE categories c SET post_count = (
    SELECT COUNT(*) FROM post_categories pc 
    INNER JOIN posts p ON pc.post_id = p.id 
    WHERE pc.category_id = c.id AND p.status = 1
);

UPDATE tags t SET post_count = (
    SELECT COUNT(*) FROM post_tags pt 
    INNER JOIN posts p ON pt.post_id = p.id 
    WHERE pt.tag_id = t.id AND p.status = 1
);

-- ============================================
-- 9. 插入测试评论
-- ============================================
INSERT INTO `comments` (`post_id`, `user_id`, `content`, `status`, `like_count`, `ip_address`) VALUES
-- 文章1的评论
(1, 4, '写得太好了！期待更多 Spring Boot 3 的实战案例', 1, 5, '192.168.1.100'),
(1, 5, '请问 Spring Boot 3 对 Spring Security 有什么影响吗？', 1, 3, '192.168.1.101'),
-- 文章2的评论
(2, 4, 'MyBatis-Plus 的代码生成器真的很好用', 1, 8, '192.168.1.100'),
(2, 5, '能不能出一期分页插件的详细教程？', 1, 2, '192.168.1.101'),
-- 文章3的评论
(3, 4, 'Composition API 比 Options API 灵活多了', 1, 10, '192.168.1.100'),
-- 文章5的评论
(5, 4, 'Cache-Aside 模式在高并发下会有什么问题吗？', 1, 6, '192.168.1.100');

-- 插入回复评论
INSERT INTO `comments` (`post_id`, `user_id`, `parent_id`, `root_id`, `reply_to_user_id`, `content`, `status`, `like_count`, `ip_address`) VALUES
-- 回复文章1的评论2
(1, 2, 2, 2, 5, 'Spring Security 6 也有重大更新，后续会专门写一篇文章介绍', 1, 2, '192.168.1.102'),
-- 回复文章2的评论4
(2, 2, 4, 4, 5, '好的，下期就出分页插件的详细教程', 1, 1, '192.168.1.102'),
-- 回复文章5的评论
(5, 2, 6, 6, 4, '高并发下要注意缓存击穿和缓存雪崩问题，可以使用分布式锁', 1, 4, '192.168.1.102');

-- 更新文章评论数
UPDATE posts p SET comment_count = (
    SELECT COUNT(*) FROM comments c 
    WHERE c.post_id = p.id AND c.status = 1
);

-- ============================================
-- 10. 插入点赞数据
-- ============================================
INSERT INTO `post_likes` (`post_id`, `user_id`) VALUES
-- 读者1点赞多篇文章
(1, 4),
(2, 4),
(3, 4),
(5, 4),
(6, 4),
-- 读者2点赞文章
(1, 5),
(2, 5),
(3, 5);

INSERT INTO `comment_likes` (`comment_id`, `user_id`) VALUES
-- 读者2点赞评论
(1, 5),
(3, 5),
(5, 5);

-- 更新点赞计数（如果没有触发器）
UPDATE posts p SET like_count = (
    SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id
);

UPDATE comments c SET like_count = (
    SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.id
);

-- ============================================
-- 11. 插入收藏数据
-- ============================================
-- 创建默认收藏夹
INSERT INTO `favorite_folders` (`user_id`, `name`, `description`, `is_default`) VALUES
(4, '默认收藏夹', '我的收藏', 1),
(4, 'Spring 学习', 'Spring 相关文章', 0),
(5, '默认收藏夹', '我的收藏', 1);

-- 添加收藏
INSERT INTO `favorites` (`user_id`, `post_id`, `folder_id`, `notes`) VALUES
(4, 1, 1, 'Spring Boot 3 必读'),
(4, 2, 2, 'MyBatis-Plus 实战'),
(4, 5, 1, 'Redis 缓存设计'),
(5, 1, 3, NULL),
(5, 3, 3, 'Vue3 学习资料');

-- 更新收藏夹文章数
UPDATE favorite_folders ff SET post_count = (
    SELECT COUNT(*) FROM favorites f WHERE f.folder_id = ff.id
);

-- 更新文章收藏数
UPDATE posts p SET favorite_count = (
    SELECT COUNT(*) FROM favorites f WHERE f.post_id = p.id
);

-- ============================================
-- 12. 插入用户关注数据
-- ============================================
INSERT INTO `user_follows` (`follower_id`, `following_id`) VALUES
(4, 2), -- 读者1 关注 作者1
(4, 3), -- 读者1 关注 作者2
(5, 2), -- 读者2 关注 作者1
(5, 3); -- 读者2 关注 作者2

-- 更新用户粉丝/关注数
UPDATE users u SET 
    follower_count = (SELECT COUNT(*) FROM user_follows uf WHERE uf.following_id = u.id),
    following_count = (SELECT COUNT(*) FROM user_follows uf WHERE uf.follower_id = u.id),
    post_count = (SELECT COUNT(*) FROM posts p WHERE p.user_id = u.id AND p.status = 1);

-- ============================================
-- 13. 插入通知数据（示例）
-- ============================================
INSERT INTO `notifications` (`user_id`, `sender_id`, `type`, `title`, `content`, `link_url`, `related_id`, `is_read`) VALUES
(2, 4, 'COMMENT', '新评论通知', '用户"读者小明"评论了您的文章', '/posts/1', 1, 0),
(2, 4, 'LIKE', '点赞通知', '用户"读者小明"点赞了您的文章', '/posts/1', 1, 0),
(2, 5, 'COMMENT', '新评论通知', '用户"读者小红"评论了您的文章', '/posts/1', 2, 0),
(5, 2, 'REPLY', '回复通知', '作者"作者一号"回复了您的评论', '/posts/1', 2, 0);

-- ============================================
-- 14. 插入系统日志（示例）
-- ============================================
INSERT INTO `operation_logs` (`user_id`, `module`, `operation`, `method`, `ip_address`, `status`) VALUES
(2, '文章管理', '创建文章', 'PostService.createPost', '192.168.1.102', 1),
(2, '文章管理', '发布文章', 'PostService.publishPost', '192.168.1.102', 1),
(4, '评论管理', '创建评论', 'CommentService.createComment', '192.168.1.100', 1),
(4, '点赞管理', '点赞文章', 'LikeService.likePost', '192.168.1.100', 1);

-- ============================================
-- 15. 验证数据
-- ============================================
SELECT '========== 数据统计 ==========' AS '';
SELECT CONCAT('用户数: ', COUNT(*)) AS result FROM users;
SELECT CONCAT('文章数: ', COUNT(*)) AS result FROM posts WHERE status = 1;
SELECT CONCAT('评论数: ', COUNT(*)) AS result FROM comments WHERE status = 1;
SELECT CONCAT('分类数: ', COUNT(*)) AS result FROM categories WHERE status = 1;
SELECT CONCAT('标签数: ', COUNT(*)) AS result FROM tags;
SELECT CONCAT('点赞数: ', COUNT(*)) AS result FROM post_likes;
SELECT CONCAT('收藏数: ', COUNT(*)) AS result FROM favorites;

-- ============================================
-- 测试查询示例
-- ============================================
-- 查看文章及其作者
SELECT p.id, p.title, u.nickname AS author, p.view_count, p.like_count, p.comment_count
FROM posts p
INNER JOIN users u ON p.user_id = u.id
WHERE p.status = 1
ORDER BY p.created_at DESC
LIMIT 5;

-- 查看评论树
SELECT 
    c.id,
    c.content,
    u.nickname AS author,
    c.parent_id,
    c.like_count
FROM comments c
INNER JOIN users u ON c.user_id = u.id
WHERE c.post_id = 1 AND c.status = 1
ORDER BY c.created_at ASC;

-- 查看热门文章
SELECT 
    p.id,
    p.title,
    p.view_count,
    p.like_count,
    p.favorite_count,
    p.comment_count,
    (p.view_count * 0.1 + p.like_count * 2 + p.favorite_count * 3 + p.comment_count * 1.5) AS hot_score
FROM posts p
WHERE p.status = 1
ORDER BY hot_score DESC
LIMIT 10;

SELECT '========== 初始化完成! ==========' AS '';