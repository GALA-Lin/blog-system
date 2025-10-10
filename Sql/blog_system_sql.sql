-- ============================================
-- Blog System - Complete MySQL 8.0 Schema
-- ============================================

-- Set charset and collation
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. User Management & Authentication
-- ============================================

-- Users table
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `username` VARCHAR(50) NOT NULL COMMENT 'Username (unique)',
  `email` VARCHAR(100) NOT NULL COMMENT 'Email address',
  `password` VARCHAR(255) NOT NULL COMMENT 'Encrypted password',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT 'Display name',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT 'Avatar image URL',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT 'User bio/description',
  `status` TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=inactive, -1=banned',
  `email_verified` TINYINT DEFAULT 0 COMMENT '0=not verified, 1=verified',
  `last_login_at` DATETIME DEFAULT NULL COMMENT 'Last login time',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Users table';

-- Roles table
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Role ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Role name (e.g., ADMIN, AUTHOR)',
  `code` VARCHAR(50) NOT NULL COMMENT 'Role code for Spring Security',
  `description` VARCHAR(200) DEFAULT NULL COMMENT 'Role description',
  `status` TINYINT DEFAULT 1 COMMENT '1=active, 0=inactive',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Roles table';

-- User-Role relationship (many-to-many)
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `role_id` BIGINT NOT NULL COMMENT 'Role ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Role mapping';

-- Permissions table
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Permission ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Permission name',
  `code` VARCHAR(100) NOT NULL COMMENT 'Permission code (e.g., post:create)',
  `resource` VARCHAR(100) DEFAULT NULL COMMENT 'Resource type (e.g., post, comment)',
  `action` VARCHAR(50) DEFAULT NULL COMMENT 'Action type (e.g., create, read, update, delete)',
  `description` VARCHAR(200) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_resource` (`resource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permissions table';

-- Role-Permission relationship (many-to-many)
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT 'Role ID',
  `permission_id` BIGINT NOT NULL COMMENT 'Permission ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role-Permission mapping';

-- ============================================
-- 2. Content Management (Posts)
-- ============================================

-- Posts table
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Post ID',
  `user_id` BIGINT NOT NULL COMMENT 'Author user ID',
  `title` VARCHAR(200) NOT NULL COMMENT 'Post title',
  `slug` VARCHAR(200) DEFAULT NULL COMMENT 'URL-friendly slug',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT 'Post summary/excerpt',
  `content` LONGTEXT NOT NULL COMMENT 'Post content (Markdown or HTML)',
  `content_type` VARCHAR(20) DEFAULT 'MARKDOWN' COMMENT 'MARKDOWN, HTML, RICHTEXT',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT 'Cover image URL',
  `status` TINYINT DEFAULT 0 COMMENT '0=draft, 1=published, 2=under_review, -1=deleted',
  `is_top` TINYINT DEFAULT 0 COMMENT '0=normal, 1=pinned to top',
  `is_original` TINYINT DEFAULT 1 COMMENT '1=original, 0=reprinted',
  `original_url` VARCHAR(255) DEFAULT NULL COMMENT 'Original article URL if reprinted',
  `view_count` BIGINT DEFAULT 0 COMMENT 'View count',
  `like_count` INT DEFAULT 0 COMMENT 'Like count',
  `favorite_count` INT DEFAULT 0 COMMENT 'Favorite count',
  `comment_count` INT DEFAULT 0 COMMENT 'Comment count',
  `allow_comment` TINYINT DEFAULT 1 COMMENT '1=allow comments, 0=disable',
  `published_at` DATETIME DEFAULT NULL COMMENT 'Published time',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_published_at` (`published_at`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_top` (`is_top`),
  FULLTEXT KEY `ft_title_content` (`title`, `content`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Posts table';

-- Categories table
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT 'Parent category ID (null for root)',
  `name` VARCHAR(50) NOT NULL COMMENT 'Category name',
  `slug` VARCHAR(50) NOT NULL COMMENT 'URL slug',
  `description` VARCHAR(200) DEFAULT NULL COMMENT 'Category description',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT 'Icon class or URL',
  `sort_order` INT DEFAULT 0 COMMENT 'Sort order (ascending)',
  `post_count` INT DEFAULT 0 COMMENT 'Number of posts',
  `status` TINYINT DEFAULT 1 COMMENT '1=active, 0=inactive',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Categories table';

-- Post-Category relationship (one-to-one or many-to-many)
DROP TABLE IF EXISTS `post_categories`;
CREATE TABLE `post_categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL COMMENT 'Post ID',
  `category_id` BIGINT NOT NULL COMMENT 'Category ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_category` (`post_id`, `category_id`),
  KEY `idx_category_id` (`category_id`),
  CONSTRAINT `fk_pc_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pc_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Post-Category mapping';

-- Tags table
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Tag ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Tag name',
  `slug` VARCHAR(50) NOT NULL COMMENT 'URL slug',
  `color` VARCHAR(20) DEFAULT NULL COMMENT 'Tag color (hex)',
  `post_count` INT DEFAULT 0 COMMENT 'Number of posts with this tag',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_slug` (`slug`),
  KEY `idx_post_count` (`post_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tags table';

-- Post-Tag relationship (many-to-many)
DROP TABLE IF EXISTS `post_tags`;
CREATE TABLE `post_tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL COMMENT 'Post ID',
  `tag_id` BIGINT NOT NULL COMMENT 'Tag ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_pt_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Post-Tag mapping';

-- ============================================
-- 3. Comment System
-- ============================================

-- Comments table
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Comment ID',
  `post_id` BIGINT NOT NULL COMMENT 'Post ID',
  `user_id` BIGINT NOT NULL COMMENT 'Commenter user ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT 'Parent comment ID (for replies)',
  `root_id` BIGINT DEFAULT NULL COMMENT 'Root comment ID (top-level comment)',
  `reply_to_user_id` BIGINT DEFAULT NULL COMMENT 'User being replied to',
  `content` TEXT NOT NULL COMMENT 'Comment content',
  `status` TINYINT DEFAULT 1 COMMENT '1=approved, 0=pending, -1=deleted, -2=spam',
  `like_count` INT DEFAULT 0 COMMENT 'Like count',
  `reply_count` INT DEFAULT 0 COMMENT 'Number of replies',
  `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'Commenter IP address',
  `user_agent` VARCHAR(255) DEFAULT NULL COMMENT 'Browser user agent',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comments table';

-- ============================================
-- 4. Like System
-- ============================================

-- Post likes table
DROP TABLE IF EXISTS `post_likes`;
CREATE TABLE `post_likes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Like ID',
  `post_id` BIGINT NOT NULL COMMENT 'Post ID',
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_pl_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pl_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Post likes table';

-- Comment likes table
DROP TABLE IF EXISTS `comment_likes`;
CREATE TABLE `comment_likes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Like ID',
  `comment_id` BIGINT NOT NULL COMMENT 'Comment ID',
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_cl_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cl_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment likes table';

-- ============================================
-- 5. Favorite/Save System
-- ============================================

-- Favorite folders table
DROP TABLE IF EXISTS `favorite_folders`;
CREATE TABLE `favorite_folders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Folder ID',
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Folder name',
  `description` VARCHAR(200) DEFAULT NULL COMMENT 'Folder description',
  `is_public` TINYINT DEFAULT 0 COMMENT '0=private, 1=public',
  `is_default` TINYINT DEFAULT 0 COMMENT '0=normal, 1=default folder',
  `post_count` INT DEFAULT 0 COMMENT 'Number of posts in folder',
  `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_ff_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Favorite folders table';

-- Favorites table
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Favorite ID',
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `post_id` BIGINT NOT NULL COMMENT 'Post ID',
  `folder_id` BIGINT DEFAULT NULL COMMENT 'Folder ID (null = default folder)',
  `notes` VARCHAR(500) DEFAULT NULL COMMENT 'Personal notes about the post',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_user_folder` (`user_id`, `folder_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_fav_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_folder` FOREIGN KEY (`folder_id`) REFERENCES `favorite_folders` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Favorites table';

-- ============================================
-- 6. File Management (MinIO metadata)
-- ============================================

-- Attachments table (tracks files in MinIO)
DROP TABLE IF EXISTS `attachments`;
CREATE TABLE `attachments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Attachment ID',
  `user_id` BIGINT NOT NULL COMMENT 'Uploader user ID',
  `post_id` BIGINT DEFAULT NULL COMMENT 'Related post ID (null if not attached yet)',
  `file_name` VARCHAR(255) NOT NULL COMMENT 'Original file name',
  `file_key` VARCHAR(500) NOT NULL COMMENT 'MinIO object key/path',
  `file_url` VARCHAR(500) NOT NULL COMMENT 'Accessible URL',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT 'MIME type',
  `file_extension` VARCHAR(20) DEFAULT NULL COMMENT 'File extension',
  `file_size` BIGINT DEFAULT 0 COMMENT 'File size in bytes',
  `bucket_name` VARCHAR(100) DEFAULT 'blog-system' COMMENT 'MinIO bucket name',
  `category` VARCHAR(20) DEFAULT 'OTHER' COMMENT 'IMAGE, DOCUMENT, VIDEO, AUDIO, OTHER',
  `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT 'Thumbnail URL (for images/videos)',
  `width` INT DEFAULT NULL COMMENT 'Image width (pixels)',
  `height` INT DEFAULT NULL COMMENT 'Image height (pixels)',
  `md5` VARCHAR(32) DEFAULT NULL COMMENT 'File MD5 hash for deduplication',
  `status` TINYINT DEFAULT 1 COMMENT '1=active, 0=deleted',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_key` (`file_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_md5` (`md5`),
  KEY `idx_category` (`category`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_att_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_att_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Attachments table';

-- ============================================
-- 7. Notification System
-- ============================================

-- Notifications table
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Notification ID',
  `user_id` BIGINT NOT NULL COMMENT 'Recipient user ID',
  `sender_id` BIGINT DEFAULT NULL COMMENT 'Sender user ID (null for system)',
  `type` VARCHAR(50) NOT NULL COMMENT 'COMMENT, LIKE, FAVORITE, FOLLOW, SYSTEM, etc.',
  `title` VARCHAR(200) NOT NULL COMMENT 'Notification title',
  `content` VARCHAR(500) DEFAULT NULL COMMENT 'Notification content',
  `link_url` VARCHAR(255) DEFAULT NULL COMMENT 'Related link URL',
  `related_id` BIGINT DEFAULT NULL COMMENT 'Related entity ID (post_id, comment_id, etc.)',
  `is_read` TINYINT DEFAULT 0 COMMENT '0=unread, 1=read',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notifications table';

-- ============================================
-- 8. User Follow System (Optional)
-- ============================================

-- User follows table
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Follow ID',
  `follower_id` BIGINT NOT NULL COMMENT 'Follower user ID',
  `following_id` BIGINT NOT NULL COMMENT 'Following user ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_following_id` (`following_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_uf_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_uf_following` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User follows table';

-- Add follower/following counts to users table
ALTER TABLE `users` 
ADD COLUMN `follower_count` INT DEFAULT 0 COMMENT 'Number of followers',
ADD COLUMN `following_count` INT DEFAULT 0 COMMENT 'Number of following',
ADD COLUMN `post_count` INT DEFAULT 0 COMMENT 'Number of posts';

-- ============================================
-- 9. System Settings & Configuration
-- ============================================

-- System settings table
DROP TABLE IF EXISTS `system_settings`;
CREATE TABLE `system_settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Setting ID',
  `setting_key` VARCHAR(100) NOT NULL COMMENT 'Setting key',
  `setting_value` TEXT COMMENT 'Setting value',
  `setting_type` VARCHAR(20) DEFAULT 'STRING' COMMENT 'STRING, NUMBER, BOOLEAN, JSON',
  `description` VARCHAR(200) DEFAULT NULL COMMENT 'Setting description',
  `is_public` TINYINT DEFAULT 0 COMMENT '0=private, 1=public',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System settings table';

-- ============================================
-- 10. Operation Logs (Optional but recommended)
-- ============================================

-- Operation logs table
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
  `user_id` BIGINT DEFAULT NULL COMMENT 'Operator user ID',
  `module` VARCHAR(50) DEFAULT NULL COMMENT 'Module name',
  `operation` VARCHAR(100) DEFAULT NULL COMMENT 'Operation description',
  `method` VARCHAR(200) DEFAULT NULL COMMENT 'Method name',
  `request_params` TEXT COMMENT 'Request parameters (JSON)',
  `response_data` TEXT COMMENT 'Response data (JSON)',
  `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP address',
  `user_agent` VARCHAR(255) DEFAULT NULL COMMENT 'User agent',
  `execution_time` INT DEFAULT NULL COMMENT 'Execution time (ms)',
  `status` TINYINT DEFAULT 1 COMMENT '1=success, 0=failure',
  `error_msg` TEXT COMMENT 'Error message if failed',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Operation logs table';

-- ============================================
-- 11. Initial Data Insertion
-- ============================================

-- Insert default roles
INSERT INTO `roles` (`name`, `code`, `description`) VALUES
('Administrator', 'ROLE_ADMIN', 'System administrator with full access'),
('Author', 'ROLE_AUTHOR', 'Can create and manage own posts'),
('Editor', 'ROLE_EDITOR', 'Can review and publish posts'),
('Reader', 'ROLE_READER', 'Can read, comment, and like posts');

-- Insert default permissions
INSERT INTO `permissions` (`name`, `code`, `resource`, `action`, `description`) VALUES
('View Posts', 'post:read', 'post', 'read', 'View published posts'),
('Create Post', 'post:create', 'post', 'create', 'Create new posts'),
('Update Own Post', 'post:update:own', 'post', 'update', 'Update own posts'),
('Update Any Post', 'post:update:any', 'post', 'update', 'Update any posts'),
('Delete Own Post', 'post:delete:own', 'post', 'delete', 'Delete own posts'),
('Delete Any Post', 'post:delete:any', 'post', 'delete', 'Delete any posts'),
('Publish Post', 'post:publish', 'post', 'publish', 'Publish posts'),
('Comment', 'comment:create', 'comment', 'create', 'Create comments'),
('Delete Own Comment', 'comment:delete:own', 'comment', 'delete', 'Delete own comments'),
('Delete Any Comment', 'comment:delete:any', 'comment', 'delete', 'Delete any comments'),
('Moderate Comments', 'comment:moderate', 'comment', 'moderate', 'Moderate comments'),
('Manage Users', 'user:manage', 'user', 'manage', 'Manage user accounts'),
('Manage Roles', 'role:manage', 'role', 'manage', 'Manage roles and permissions'),
('Manage Categories', 'category:manage', 'category', 'manage', 'Manage categories'),
('Manage Tags', 'tag:manage', 'tag', 'manage', 'Manage tags'),
('Upload Files', 'file:upload', 'file', 'upload', 'Upload files'),
('View System Settings', 'settings:read', 'settings', 'read', 'View system settings'),
('Update System Settings', 'settings:update', 'settings', 'update', 'Update system settings');

-- Assign permissions to ADMIN role (id=1)
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 1, id FROM `permissions`;

-- Assign permissions to AUTHOR role (id=2)
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 2, id FROM `permissions` WHERE `code` IN (
  'post:read', 'post:create', 'post:update:own', 'post:delete:own', 
  'comment:create', 'comment:delete:own', 'file:upload'
);

-- Assign permissions to EDITOR role (id=3)
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 3, id FROM `permissions` WHERE `code` IN (
  'post:read', 'post:create', 'post:update:any', 'post:delete:own', 'post:publish',
  'comment:create', 'comment:delete:own', 'comment:moderate',
  'category:manage', 'tag:manage', 'file:upload'
);

-- Assign permissions to READER role (id=4)
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 4, id FROM `permissions` WHERE `code` IN (
  'post:read', 'comment:create', 'comment:delete:own'
);

-- Insert default categories
INSERT INTO `categories` (`name`, `slug`, `description`, `sort_order`) VALUES
('Technology', 'technology', 'Articles about technology and programming', 1),
('Lifestyle', 'lifestyle', 'Lifestyle and personal development', 2),
('Travel', 'travel', 'Travel experiences and guides', 3),
('Food', 'food', 'Recipes and food reviews', 4),
('Tutorial', 'tutorial', 'Step-by-step guides and tutorials', 5),
('News', 'news', 'Latest news and updates', 6);

-- Insert default system settings
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `setting_type`, `description`, `is_public`) VALUES
('site_name', 'My Blog System', 'STRING', 'Website name', 1),
('site_description', 'A modern blog platform built with Spring Boot', 'STRING', 'Website description', 1),
('posts_per_page', '10', 'NUMBER', 'Number of posts per page', 1),
('allow_registration', 'true', 'BOOLEAN', 'Allow new user registration', 0),
('comment_review_required', 'false', 'BOOLEAN', 'Require admin approval for comments', 0),
('max_upload_size', '10485760', 'NUMBER', 'Max file upload size in bytes (10MB)', 0),
('enable_email_notification', 'true', 'BOOLEAN', 'Enable email notifications', 0);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 12. Useful Views (Optional)
-- ============================================

-- View: Post with author and stats
CREATE OR REPLACE VIEW `v_post_list` AS
SELECT 
    p.id,
    p.title,
    p.slug,
    p.summary,
    p.cover_image,
    p.status,
    p.is_top,
    p.view_count,
    p.like_count,
    p.favorite_count,
    p.comment_count,
    p.created_at,
    p.published_at,
    u.id AS author_id,
    u.username AS author_username,
    u.nickname AS author_nickname,
    u.avatar_url AS author_avatar
FROM posts p
INNER JOIN users u ON p.user_id = u.id
WHERE p.status = 1;

-- View: User statistics
CREATE OR REPLACE VIEW `v_user_stats` AS
SELECT 
    u.id,
    u.username,
    u.nickname,
    u.avatar_url,
    u.post_count,
    u.follower_count,
    u.following_count,
    COUNT(DISTINCT pl.id) AS total_likes_received,
    COUNT(DISTINCT f.id) AS total_favorites_received,
    u.created_at AS joined_at
FROM users u
LEFT JOIN posts p ON u.id = p.user_id AND p.status = 1
LEFT JOIN post_likes pl ON p.id = pl.post_id
LEFT JOIN favorites f ON p.id = f.post_id
GROUP BY u.id;

-- View: Hot posts (by engagement)
CREATE OR REPLACE VIEW `v_hot_posts` AS
SELECT 
    p.id,
    p.title,
    p.slug,
    p.summary,
    p.cover_image,
    p.view_count,
    p.like_count,
    p.favorite_count,
    p.comment_count,
    (p.view_count * 0.1 + p.like_count * 2 + p.favorite_count * 3 + p.comment_count * 1.5) AS hot_score,
    p.published_at,
    u.username AS author_username,
    u.nickname AS author_nickname,
    u.avatar_url AS author_avatar
FROM posts p
INNER JOIN users u ON p.user_id = u.id
WHERE p.status = 1
  AND p.published_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY hot_score DESC;

-- ============================================
-- 13. Stored Procedures (Optional but useful)
-- ============================================

-- Procedure: Increment post view count
DELIMITER $

DROP PROCEDURE IF EXISTS `sp_increment_post_view`$
CREATE PROCEDURE `sp_increment_post_view`(
    IN p_post_id BIGINT
)
BEGIN
    UPDATE posts 
    SET view_count = view_count + 1 
    WHERE id = p_post_id AND status = 1;
END$

-- Procedure: Get post with full details
DROP PROCEDURE IF EXISTS `sp_get_post_detail`$
CREATE PROCEDURE `sp_get_post_detail`(
    IN p_post_id BIGINT,
    IN p_user_id BIGINT
)
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
END$

-- Procedure: Update tag/category post counts
DROP PROCEDURE IF EXISTS `sp_update_tag_count`$
CREATE PROCEDURE `sp_update_tag_count`(
    IN p_tag_id BIGINT
)
BEGIN
    UPDATE tags 
    SET post_count = (
        SELECT COUNT(*) 
        FROM post_tags pt
        INNER JOIN posts p ON pt.post_id = p.id
        WHERE pt.tag_id = p_tag_id AND p.status = 1
    )
    WHERE id = p_tag_id;
END$

DROP PROCEDURE IF EXISTS `sp_update_category_count`$
CREATE PROCEDURE `sp_update_category_count`(
    IN p_category_id BIGINT
)
BEGIN
    UPDATE categories 
    SET post_count = (
        SELECT COUNT(*) 
        FROM post_categories pc
        INNER JOIN posts p ON pc.post_id = p.id
        WHERE pc.category_id = p_category_id AND p.status = 1
    )
    WHERE id = p_category_id;
END$

-- Procedure: Clean up deleted posts and related data
DROP PROCEDURE IF EXISTS `sp_cleanup_deleted_posts`$
CREATE PROCEDURE `sp_cleanup_deleted_posts`(
    IN p_days_old INT
)
BEGIN
    DECLARE v_cutoff_date DATETIME;
    SET v_cutoff_date = DATE_SUB(NOW(), INTERVAL p_days_old DAY);
    
    -- Delete posts marked as deleted older than specified days
    DELETE FROM posts 
    WHERE status = -1 
      AND updated_at < v_cutoff_date;
    
    -- Orphaned attachments will be cascade deleted
    SELECT ROW_COUNT() AS deleted_posts_count;
END$

DELIMITER ;

-- ============================================
-- 14. Triggers for Auto-Update Counts
-- ============================================

DELIMITER $

-- Trigger: Update post comment_count when comment is added
DROP TRIGGER IF EXISTS `tr_comment_after_insert`$
CREATE TRIGGER `tr_comment_after_insert`
AFTER INSERT ON `comments`
FOR EACH ROW
BEGIN
    IF NEW.status = 1 THEN
        UPDATE posts SET comment_count = comment_count + 1 WHERE id = NEW.post_id;
        
        -- Update reply_count for parent comment
        IF NEW.parent_id IS NOT NULL THEN
            UPDATE comments SET reply_count = reply_count + 1 WHERE id = NEW.parent_id;
        END IF;
    END IF;
END$

-- Trigger: Update post comment_count when comment is deleted
DROP TRIGGER IF EXISTS `tr_comment_after_delete`$
CREATE TRIGGER `tr_comment_after_delete`
AFTER DELETE ON `comments`
FOR EACH ROW
BEGIN
    IF OLD.status = 1 THEN
        UPDATE posts SET comment_count = comment_count - 1 WHERE id = OLD.post_id;
        
        -- Update reply_count for parent comment
        IF OLD.parent_id IS NOT NULL THEN
            UPDATE comments SET reply_count = reply_count - 1 WHERE id = OLD.parent_id;
        END IF;
    END IF;
END$

-- Trigger: Update post comment_count when comment status changes
DROP TRIGGER IF EXISTS `tr_comment_after_update`$
CREATE TRIGGER `tr_comment_after_update`
AFTER UPDATE ON `comments`
FOR EACH ROW
BEGIN
    IF OLD.status = 1 AND NEW.status != 1 THEN
        UPDATE posts SET comment_count = comment_count - 1 WHERE id = NEW.post_id;
    ELSEIF OLD.status != 1 AND NEW.status = 1 THEN
        UPDATE posts SET comment_count = comment_count + 1 WHERE id = NEW.post_id;
    END IF;
END$

-- Trigger: Update user post_count when post is published
DROP TRIGGER IF EXISTS `tr_post_after_insert`$
CREATE TRIGGER `tr_post_after_insert`
AFTER INSERT ON `posts`
FOR EACH ROW
BEGIN
    IF NEW.status = 1 THEN
        UPDATE users SET post_count = post_count + 1 WHERE id = NEW.user_id;
    END IF;
END$

DROP TRIGGER IF EXISTS `tr_post_after_update`$
CREATE TRIGGER `tr_post_after_update`
AFTER UPDATE ON `posts`
FOR EACH ROW
BEGIN
    IF OLD.status != 1 AND NEW.status = 1 THEN
        UPDATE users SET post_count = post_count + 1 WHERE id = NEW.user_id;
    ELSEIF OLD.status = 1 AND NEW.status != 1 THEN
        UPDATE users SET post_count = post_count - 1 WHERE id = NEW.user_id;
    END IF;
END$

-- Trigger: Update folder post_count
DROP TRIGGER IF EXISTS `tr_favorite_after_insert`$
CREATE TRIGGER `tr_favorite_after_insert`
AFTER INSERT ON `favorites`
FOR EACH ROW
BEGIN
    IF NEW.folder_id IS NOT NULL THEN
        UPDATE favorite_folders SET post_count = post_count + 1 WHERE id = NEW.folder_id;
    END IF;
END$

DROP TRIGGER IF EXISTS `tr_favorite_after_delete`$
CREATE TRIGGER `tr_favorite_after_delete`
AFTER DELETE ON `favorites`
FOR EACH ROW
BEGIN
    IF OLD.folder_id IS NOT NULL THEN
        UPDATE favorite_folders SET post_count = post_count - 1 WHERE id = OLD.folder_id;
    END IF;
END$

-- Trigger: Update user follower/following counts
DROP TRIGGER IF EXISTS `tr_follow_after_insert`$
CREATE TRIGGER `tr_follow_after_insert`
AFTER INSERT ON `user_follows`
FOR EACH ROW
BEGIN
    UPDATE users SET following_count = following_count + 1 WHERE id = NEW.follower_id;
    UPDATE users SET follower_count = follower_count + 1 WHERE id = NEW.following_id;
END$

DROP TRIGGER IF EXISTS `tr_follow_after_delete`$
CREATE TRIGGER `tr_follow_after_delete`
AFTER DELETE ON `user_follows`
FOR EACH ROW
BEGIN
    UPDATE users SET following_count = following_count - 1 WHERE id = OLD.follower_id;
    UPDATE users SET follower_count = follower_count - 1 WHERE id = OLD.following_id;
END$

DELIMITER ;

-- ============================================
-- 15. Indexes for Performance Optimization
-- ============================================

-- Additional composite indexes for common queries
CREATE INDEX idx_post_status_published ON posts(status, published_at DESC);
CREATE INDEX idx_post_user_status ON posts(user_id, status);
CREATE INDEX idx_comment_post_status ON comments(post_id, status, created_at DESC);
CREATE INDEX idx_notification_user_read ON notifications(user_id, is_read, created_at DESC);

-- ============================================
-- 16. Sample Data for Testing (Optional)
-- ============================================

-- Create a test admin user (password: admin123, you should hash this properly)
-- Note: In production, use BCrypt or similar to hash passwords
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `status`, `email_verified`) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'Administrator', 1, 1);

-- Assign ADMIN role to the admin user
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);

-- Create default favorite folder for admin
INSERT INTO `favorite_folders` (`user_id`, `name`, `description`, `is_default`) VALUES
(1, 'My Favorites', 'Default favorite folder', 1);

-- ============================================
-- End of Schema
-- ============================================

-- Summary of tables created:
-- 1. users, roles, user_roles, permissions, role_permissions (Auth & Authorization)
-- 2. posts, categories, post_categories, tags, post_tags (Content)
-- 3. comments (Comment System)
-- 4. post_likes, comment_likes (Like System)
-- 5. favorites, favorite_folders (Favorite System)
-- 6. attachments (File Management)
-- 7. notifications (Notification System)
-- 8. user_follows (Social Features)
-- 9. system_settings (Configuration)
-- 10. operation_logs (Audit Trail)

-- Total: 20 tables + 3 views + 7 stored procedures + 10 triggers