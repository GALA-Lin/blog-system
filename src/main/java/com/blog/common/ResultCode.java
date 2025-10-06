package com.blog.common;

import lombok.Getter;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-14:39
 * @Description: 定义业务返回码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),

    // Authentication & Authorization
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "访问被拒绝"),
    TOKEN_EXPIRED(401, "令牌已过期"),
    TOKEN_INVALID(401, "无效的令牌"),

    // User Related
    USER_NOT_FOUND(404, "用户未找到"),
    USER_ALREADY_EXISTS(400, "用户已存在"),
    USER_DISABLED(403, "账户已禁用"),
    INVALID_CREDENTIALS(401, "无效的用户名或密码"),
    PASSWORD_NOT_MATCH(400, "密码不匹配"),

    // Post Related
    POST_NOT_FOUND(404, "文章未找到"),
    POST_ALREADY_EXISTS(400, "文章已存在"),

    // Comment Related
    COMMENT_NOT_FOUND(404, "评论未找到"),
    COMMENT_TOO_LONG(400, "评论过长"),
    COMMENT_DEPTH_EXCEEDED(400, "评论嵌套深度超出限制"),

    // File Related
    FILE_UPLOAD_FAILED(500, "文件上传失败"),
    FILE_NOT_FOUND(404, "文件未找到"),
    FILE_TYPE_NOT_ALLOWED(400, "不允许的文件类型"),
    FILE_SIZE_EXCEEDED(400, "文件大小超出限制"),

    // Validation
    VALIDATE_FAILED(400, "验证失败"),
    PARAM_ERROR(400, "参数错误"),

    // System
    SYSTEM_ERROR(500, "系统错误"),
    RESOURCE_NOT_FOUND(404, "资源未找到");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
