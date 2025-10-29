/*
 Navicat Premium Dump SQL

 Source Server         : blog_system
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3307
 Source Schema         : blog_system

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 28/10/2025 17:16:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for attachments
-- ----------------------------
DROP TABLE IF EXISTS `attachments`;
CREATE TABLE `attachments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Attachment ID',
  `user_id` bigint NOT NULL COMMENT 'Uploader user ID',
  `post_id` bigint NULL DEFAULT NULL COMMENT 'Related post ID (null if not attached yet)',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Original file name',
  `file_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MinIO object key/path',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Accessible URL',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MIME type',
  `file_extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'File extension',
  `file_size` bigint NULL DEFAULT 0 COMMENT 'File size in bytes',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'blog-system' COMMENT 'MinIO bucket name',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'OTHER' COMMENT 'IMAGE, DOCUMENT, VIDEO, AUDIO, OTHER',
  `thumbnail_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Thumbnail URL (for images/videos)',
  `width` int NULL DEFAULT NULL COMMENT 'Image width (pixels)',
  `height` int NULL DEFAULT NULL COMMENT 'Image height (pixels)',
  `md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'File MD5 hash for deduplication',
  `status` tinyint NULL DEFAULT 1 COMMENT '1=active, 0=deleted',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_file_key`(`file_key` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_md5`(`md5` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_att_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_att_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Attachments table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT 'Parent category ID (null for root)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Category name',
  `slug` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'URL slug',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Category description',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Icon class or URL',
  `sort_order` int NULL DEFAULT 0 COMMENT 'Sort order (ascending)',
  `post_count` int NULL DEFAULT 0 COMMENT 'Number of posts',
  `status` tinyint NULL DEFAULT 1 COMMENT '1=active, 0=inactive',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_slug`(`slug` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Categories table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment_likes
-- ----------------------------
DROP TABLE IF EXISTS `comment_likes`;
CREATE TABLE `comment_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Like ID',
  `comment_id` bigint NOT NULL COMMENT 'Comment ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_cl_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_cl_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Comment likes table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Comment ID',
  `post_id` bigint NOT NULL COMMENT 'Post ID',
  `user_id` bigint NOT NULL COMMENT 'Commenter user ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT 'Parent comment ID (for replies)',
  `root_id` bigint NULL DEFAULT NULL COMMENT 'Root comment ID (top-level comment)',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT 'User being replied to',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Comment content',
  `status` tinyint NULL DEFAULT 1 COMMENT '1=approved, 0=pending, -1=deleted, -2=spam',
  `like_count` int NULL DEFAULT 0 COMMENT 'Like count',
  `reply_count` int NULL DEFAULT 0 COMMENT 'Number of replies',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Commenter IP address',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Browser user agent',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_root_id`(`root_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_comment_post_status`(`post_id` ASC, `status` ASC, `created_at` DESC) USING BTREE,
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Comments table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for favorite_folders
-- ----------------------------
DROP TABLE IF EXISTS `favorite_folders`;
CREATE TABLE `favorite_folders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Folder ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Folder name',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Folder description',
  `is_public` tinyint NULL DEFAULT 0 COMMENT '0=private, 1=public',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '0=normal, 1=default folder',
  `post_count` int NULL DEFAULT 0 COMMENT 'Number of posts in folder',
  `sort_order` int NULL DEFAULT 0 COMMENT 'Sort order',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  CONSTRAINT `fk_ff_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Favorite folders table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Favorite ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `post_id` bigint NOT NULL COMMENT 'Post ID',
  `folder_id` bigint NULL DEFAULT NULL COMMENT 'Folder ID (null = default folder)',
  `notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Personal notes about the post',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_post`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `idx_folder_id`(`folder_id` ASC) USING BTREE,
  INDEX `idx_user_folder`(`user_id` ASC, `folder_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `fk_fav_post`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_fav_folder` FOREIGN KEY (`folder_id`) REFERENCES `favorite_folders` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_fav_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Favorites table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for file_uploads
-- ----------------------------
DROP TABLE IF EXISTS `file_uploads`;
CREATE TABLE `file_uploads`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `stored_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件访问URL',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件分类：IMAGE, DOCUMENT, VIDEO, AUDIO, OTHER',
  `storage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MINIO' COMMENT '存储类型：LOCAL, MINIO, OSS',
  `md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件MD5值',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联实体类型',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联实体ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=正常，0=待确认，-1=已删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_md5`(`md5` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_related`(`related_type` ASC, `related_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_file_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件上传表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Notification ID',
  `user_id` bigint NOT NULL COMMENT 'Recipient user ID',
  `sender_id` bigint NULL DEFAULT NULL COMMENT 'Sender user ID (null for system)',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'COMMENT, LIKE, FAVORITE, FOLLOW, SYSTEM, etc.',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Notification title',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Notification content',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Related link URL',
  `related_id` bigint NULL DEFAULT NULL COMMENT 'Related entity ID (post_id, comment_id, etc.)',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '0=unread, 1=read',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_notification_user_read`(`user_id` ASC, `is_read` ASC, `created_at` DESC) USING BTREE,
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Notifications table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT 'Operator user ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Module name',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Operation description',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Method name',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Request parameters (JSON)',
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Response data (JSON)',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP address',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'User agent',
  `execution_time` int NULL DEFAULT NULL COMMENT 'Execution time (ms)',
  `status` tinyint NULL DEFAULT 1 COMMENT '1=success, 0=failure',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Error message if failed',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Operation logs table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Permission ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Permission name',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Permission code (e.g., post:create)',
  `resource` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Resource type (e.g., post, comment)',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Action type (e.g., create, read, update, delete)',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_resource`(`resource` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Permissions table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_categories
-- ----------------------------
DROP TABLE IF EXISTS `post_categories`;
CREATE TABLE `post_categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT 'Post ID',
  `category_id` bigint NOT NULL COMMENT 'Category ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_category`(`post_id` ASC, `category_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  CONSTRAINT `fk_pc_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pc_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Post-Category mapping' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_likes
-- ----------------------------
DROP TABLE IF EXISTS `post_likes`;
CREATE TABLE `post_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Like ID',
  `post_id` bigint NOT NULL COMMENT 'Post ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_pl_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pl_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Post likes table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_tags
-- ----------------------------
DROP TABLE IF EXISTS `post_tags`;
CREATE TABLE `post_tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT 'Post ID',
  `tag_id` bigint NOT NULL COMMENT 'Tag ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_tag`(`post_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `fk_pt_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Post-Tag mapping' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Post ID',
  `user_id` bigint NOT NULL COMMENT 'Author user ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Post title',
  `slug` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'URL-friendly slug',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Post summary/excerpt',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Post content (Markdown or HTML)',
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'MARKDOWN' COMMENT 'MARKDOWN, HTML, RICHTEXT',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Cover image URL',
  `status` tinyint NULL DEFAULT 0 COMMENT '0=draft, 1=published, 2=under_review, -1=deleted',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '0=normal, 1=pinned to top',
  `is_original` tinyint NULL DEFAULT 1 COMMENT '1=original, 0=reprinted',
  `original_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Original article URL if reprinted',
  `view_count` bigint NULL DEFAULT 0 COMMENT 'View count',
  `like_count` int NULL DEFAULT 0 COMMENT 'Like count',
  `favorite_count` int NULL DEFAULT 0 COMMENT 'Favorite count',
  `comment_count` int NULL DEFAULT 0 COMMENT 'Comment count',
  `allow_comment` tinyint NULL DEFAULT 1 COMMENT '1=allow comments, 0=disable',
  `published_at` datetime NULL DEFAULT NULL COMMENT 'Published time',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_slug`(`slug` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_published_at`(`published_at` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_is_top`(`is_top` ASC) USING BTREE,
  INDEX `idx_post_status_published`(`status` ASC, `published_at` DESC) USING BTREE,
  INDEX `idx_post_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  FULLTEXT INDEX `ft_title_content`(`title`, `content`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Posts table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT 'Role ID',
  `permission_id` bigint NOT NULL COMMENT 'Permission ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE,
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Role-Permission mapping' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Role ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role name (e.g., ADMIN, AUTHOR)',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role code for Spring Security',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Role description',
  `status` tinyint NULL DEFAULT 1 COMMENT '1=active, 0=inactive',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Roles table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_settings
-- ----------------------------
DROP TABLE IF EXISTS `system_settings`;
CREATE TABLE `system_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Setting ID',
  `setting_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Setting key',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Setting value',
  `setting_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'STRING' COMMENT 'STRING, NUMBER, BOOLEAN, JSON',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Setting description',
  `is_public` tinyint NULL DEFAULT 0 COMMENT '0=private, 1=public',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_setting_key`(`setting_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'System settings table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Tag ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Tag name',
  `slug` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'URL slug',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Tag color (hex)',
  `post_count` int NULL DEFAULT 0 COMMENT 'Number of posts with this tag',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  UNIQUE INDEX `uk_slug`(`slug` ASC) USING BTREE,
  INDEX `idx_post_count`(`post_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Tags table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Follow ID',
  `follower_id` bigint NOT NULL COMMENT 'Follower user ID',
  `following_id` bigint NOT NULL COMMENT 'Following user ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follower_following`(`follower_id` ASC, `following_id` ASC) USING BTREE,
  INDEX `idx_following_id`(`following_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_uf_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_uf_following` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User follows table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `role_id` bigint NOT NULL COMMENT 'Role ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User-Role mapping' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Username (unique)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Email address',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Encrypted password',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Display name',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Avatar image URL',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'User bio/description',
  `status` tinyint NULL DEFAULT 1 COMMENT 'Status: 1=active, 0=inactive, -1=banned',
  `email_verified` tinyint NULL DEFAULT 0 COMMENT '0=not verified, 1=verified',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT 'Last login time',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  `follower_count` int NULL DEFAULT 0 COMMENT 'Number of followers',
  `following_count` int NULL DEFAULT 0 COMMENT 'Number of following',
  `post_count` int NULL DEFAULT 0 COMMENT 'Number of posts',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Users table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- View structure for v_hot_posts
-- ----------------------------
DROP VIEW IF EXISTS `v_hot_posts`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_hot_posts` AS select `p`.`id` AS `id`,`p`.`title` AS `title`,`p`.`slug` AS `slug`,`p`.`summary` AS `summary`,`p`.`cover_image` AS `cover_image`,`p`.`view_count` AS `view_count`,`p`.`like_count` AS `like_count`,`p`.`favorite_count` AS `favorite_count`,`p`.`comment_count` AS `comment_count`,((((`p`.`view_count` * 0.1) + (`p`.`like_count` * 2)) + (`p`.`favorite_count` * 3)) + (`p`.`comment_count` * 1.5)) AS `hot_score`,`p`.`published_at` AS `published_at`,`u`.`username` AS `author_username`,`u`.`nickname` AS `author_nickname`,`u`.`avatar_url` AS `author_avatar` from (`posts` `p` join `users` `u` on((`p`.`user_id` = `u`.`id`))) where ((`p`.`status` = 1) and (`p`.`published_at` >= (now() - interval 30 day))) order by ((((`p`.`view_count` * 0.1) + (`p`.`like_count` * 2)) + (`p`.`favorite_count` * 3)) + (`p`.`comment_count` * 1.5)) desc;

-- ----------------------------
-- View structure for v_post_list
-- ----------------------------
DROP VIEW IF EXISTS `v_post_list`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_post_list` AS select `p`.`id` AS `id`,`p`.`title` AS `title`,`p`.`slug` AS `slug`,`p`.`summary` AS `summary`,`p`.`cover_image` AS `cover_image`,`p`.`status` AS `status`,`p`.`is_top` AS `is_top`,`p`.`view_count` AS `view_count`,`p`.`like_count` AS `like_count`,`p`.`favorite_count` AS `favorite_count`,`p`.`comment_count` AS `comment_count`,`p`.`created_at` AS `created_at`,`p`.`published_at` AS `published_at`,`u`.`id` AS `author_id`,`u`.`username` AS `author_username`,`u`.`nickname` AS `author_nickname`,`u`.`avatar_url` AS `author_avatar` from (`posts` `p` join `users` `u` on((`p`.`user_id` = `u`.`id`))) where (`p`.`status` = 1);

-- ----------------------------
-- View structure for v_user_stats
-- ----------------------------
DROP VIEW IF EXISTS `v_user_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_user_stats` AS select `u`.`id` AS `id`,`u`.`username` AS `username`,`u`.`nickname` AS `nickname`,`u`.`avatar_url` AS `avatar_url`,`u`.`post_count` AS `post_count`,`u`.`follower_count` AS `follower_count`,`u`.`following_count` AS `following_count`,count(distinct `pl`.`id`) AS `total_likes_received`,count(distinct `f`.`id`) AS `total_favorites_received`,`u`.`created_at` AS `joined_at` from (((`users` `u` left join `posts` `p` on(((`u`.`id` = `p`.`user_id`) and (`p`.`status` = 1)))) left join `post_likes` `pl` on((`p`.`id` = `pl`.`post_id`))) left join `favorites` `f` on((`p`.`id` = `f`.`post_id`))) group by `u`.`id`;

-- ----------------------------
-- Procedure structure for sp_cleanup_deleted_posts
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_cleanup_deleted_posts`;
delimiter ;;
CREATE PROCEDURE `sp_cleanup_deleted_posts`(IN p_days_old INT)
BEGIN
    DECLARE v_cutoff_date DATETIME;
    SET v_cutoff_date = DATE_SUB(NOW(), INTERVAL p_days_old DAY);
    
    -- Delete posts marked as deleted older than specified days
    DELETE FROM posts 
    WHERE status = -1 
      AND updated_at < v_cutoff_date;
    
    -- Orphaned attachments will be cascade deleted
    SELECT ROW_COUNT() AS deleted_posts_count;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_get_post_detail
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_get_post_detail`;
delimiter ;;
CREATE PROCEDURE `sp_get_post_detail`(IN p_post_id BIGINT,
    IN p_user_id BIGINT)
BEGIN
    -- Main post data
    SELECT 
        p.*,
        u.username AS author_username,
        u.nickname AS author_nickname,
        u.avatar_url AS author_avatar,
        u.bio AS author_bio,
        -- Check if current user liked this post
        EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.id AND user_id = p_user_id) AS is_liked,
        -- Check if current user favorited this post
        EXISTS(SELECT 1 FROM favorites WHERE post_id = p.id AND user_id = p_user_id) AS is_favorited,
        -- Get folder_id if favorited
        (SELECT folder_id FROM favorites WHERE post_id = p.id AND user_id = p_user_id) AS favorite_folder_id
    FROM posts p
    INNER JOIN users u ON p.user_id = u.id
    WHERE p.id = p_post_id;
    
    -- Categories
    SELECT c.* 
    FROM categories c
    INNER JOIN post_categories pc ON c.id = pc.category_id
    WHERE pc.post_id = p_post_id;
    
    -- Tags
    SELECT t.* 
    FROM tags t
    INNER JOIN post_tags pt ON t.id = pt.tag_id
    WHERE pt.post_id = p_post_id;
    
    -- Attachments
    SELECT * 
    FROM attachments 
    WHERE post_id = p_post_id AND status = 1
    ORDER BY created_at;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_increment_post_view
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_increment_post_view`;
delimiter ;;
CREATE PROCEDURE `sp_increment_post_view`(IN p_post_id BIGINT)
BEGIN
    UPDATE posts 
    SET view_count = view_count + 1 
    WHERE id = p_post_id AND status = 1;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_update_category_count
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_update_category_count`;
delimiter ;;
CREATE PROCEDURE `sp_update_category_count`(IN p_category_id BIGINT)
BEGIN
    UPDATE categories 
    SET post_count = (
        SELECT COUNT(*) 
        FROM post_categories pc
        INNER JOIN posts p ON pc.post_id = p.id
        WHERE pc.category_id = p_category_id AND p.status = 1
    )
    WHERE id = p_category_id;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_update_tag_count
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_update_tag_count`;
delimiter ;;
CREATE PROCEDURE `sp_update_tag_count`(IN p_tag_id BIGINT)
BEGIN
    UPDATE tags 
    SET post_count = (
        SELECT COUNT(*) 
        FROM post_tags pt
        INNER JOIN posts p ON pt.post_id = p.id
        WHERE pt.tag_id = p_tag_id AND p.status = 1
    )
    WHERE id = p_tag_id;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table comments
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_comment_after_insert`;
delimiter ;;
CREATE TRIGGER `tr_comment_after_insert` AFTER INSERT ON `comments` FOR EACH ROW BEGIN
    -- 只更新文章表的评论数
    IF NEW.status = 1 THEN
        UPDATE posts SET comment_count = comment_count + 1 WHERE id = NEW.post_id;
    END IF;
    
    -- ❌ 删除这部分：不要在触发器中更新 comments 表
    -- IF NEW.parent_id IS NOT NULL THEN
    --     UPDATE comments SET reply_count = reply_count + 1 WHERE id = NEW.parent_id;
    -- END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table comments
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_comment_after_delete`;
delimiter ;;
CREATE TRIGGER `tr_comment_after_delete` AFTER DELETE ON `comments` FOR EACH ROW BEGIN
    IF OLD.status = 1 THEN
        UPDATE posts SET comment_count = comment_count - 1 WHERE id = OLD.post_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table comments
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_comment_after_update`;
delimiter ;;
CREATE TRIGGER `tr_comment_after_update` AFTER UPDATE ON `comments` FOR EACH ROW BEGIN
    -- 只处理状态变化
    IF OLD.status = 1 AND NEW.status != 1 THEN
        UPDATE posts SET comment_count = comment_count - 1 WHERE id = NEW.post_id;
    ELSEIF OLD.status != 1 AND NEW.status = 1 THEN
        UPDATE posts SET comment_count = comment_count + 1 WHERE id = NEW.post_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table favorites
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_favorite_after_insert`;
delimiter ;;
CREATE TRIGGER `tr_favorite_after_insert` AFTER INSERT ON `favorites` FOR EACH ROW BEGIN
    IF NEW.folder_id IS NOT NULL THEN
        UPDATE favorite_folders SET post_count = post_count + 1 WHERE id = NEW.folder_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table favorites
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_favorite_after_delete`;
delimiter ;;
CREATE TRIGGER `tr_favorite_after_delete` AFTER DELETE ON `favorites` FOR EACH ROW BEGIN
    IF OLD.folder_id IS NOT NULL THEN
        UPDATE favorite_folders SET post_count = post_count - 1 WHERE id = OLD.folder_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table posts
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_post_after_insert`;
delimiter ;;
CREATE TRIGGER `tr_post_after_insert` AFTER INSERT ON `posts` FOR EACH ROW BEGIN
    IF NEW.status = 1 THEN
        UPDATE users SET post_count = post_count + 1 WHERE id = NEW.user_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table posts
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_post_after_update`;
delimiter ;;
CREATE TRIGGER `tr_post_after_update` AFTER UPDATE ON `posts` FOR EACH ROW BEGIN
    IF OLD.status != 1 AND NEW.status = 1 THEN
        UPDATE users SET post_count = post_count + 1 WHERE id = NEW.user_id;
    ELSEIF OLD.status = 1 AND NEW.status != 1 THEN
        UPDATE users SET post_count = post_count - 1 WHERE id = NEW.user_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user_follows
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_follow_after_insert`;
delimiter ;;
CREATE TRIGGER `tr_follow_after_insert` AFTER INSERT ON `user_follows` FOR EACH ROW BEGIN
    UPDATE users SET following_count = following_count + 1 WHERE id = NEW.follower_id;
    UPDATE users SET follower_count = follower_count + 1 WHERE id = NEW.following_id;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user_follows
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_follow_after_delete`;
delimiter ;;
CREATE TRIGGER `tr_follow_after_delete` AFTER DELETE ON `user_follows` FOR EACH ROW BEGIN
    UPDATE users SET following_count = following_count - 1 WHERE id = OLD.follower_id;
    UPDATE users SET follower_count = follower_count - 1 WHERE id = OLD.following_id;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
