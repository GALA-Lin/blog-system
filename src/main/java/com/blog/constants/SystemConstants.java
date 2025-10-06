package com.blog.constants;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-15:33
 * @Description: 系统常量
 */
public interface SystemConstants {

    // User Status
    int USER_STATUS_ACTIVE = 1;
    int USER_STATUS_INACTIVE = 0;
    int USER_STATUS_BANNED = -1;

    // Post Status
    int POST_STATUS_DRAFT = 0;
    int POST_STATUS_PUBLISHED = 1;
    int POST_STATUS_UNDER_REVIEW = 2;
    int POST_STATUS_DELETED = -1;

    // Comment Status
    int COMMENT_STATUS_APPROVED = 1;
    int COMMENT_STATUS_PENDING = 0;
    int COMMENT_STATUS_DELETED = -1;
    int COMMENT_STATUS_SPAM = -2;

    // Content Type
    String CONTENT_TYPE_MARKDOWN = "MARKDOWN";
    String CONTENT_TYPE_HTML = "HTML";
    String CONTENT_TYPE_RICHTEXT = "RICHTEXT";

    // File Category
    String FILE_CATEGORY_IMAGE = "IMAGE";
    String FILE_CATEGORY_DOCUMENT = "DOCUMENT";
    String FILE_CATEGORY_VIDEO = "VIDEO";
    String FILE_CATEGORY_AUDIO = "AUDIO";
    String FILE_CATEGORY_OTHER = "OTHER";

    // Cache Keys
    String CACHE_POST_PREFIX = "post:";
    String CACHE_USER_PREFIX = "user:";
    String CACHE_COMMENT_PREFIX = "comment:";
    String CACHE_HOT_POSTS = "posts:hot";
    String CACHE_CATEGORY_TREE = "category:tree";

    // Redis Keys for Like System
    String KEY_USER_LIKED_POSTS = "user:%d:liked:posts";
    String KEY_USER_LIKED_COMMENTS = "user:%d:liked:comments";
    String KEY_POST_LIKE_COUNT = "post:%d:like_count";
    String KEY_COMMENT_LIKE_COUNT = "comment:%d:like_count";

    // Redis Keys for Favorite System
    String KEY_USER_FAVORITED_POSTS = "user:%d:favorited:posts";
    String KEY_POST_FAVORITE_COUNT = "post:%d:favorite_count";

    // Login Attempts
    String KEY_LOGIN_ATTEMPTS = "login:attempts:%s";
    String KEY_LOGIN_LOCKED = "login:locked:%s";

    // Default Values
    int DEFAULT_PAGE_SIZE = 10;
    int MAX_PAGE_SIZE = 100;
}
