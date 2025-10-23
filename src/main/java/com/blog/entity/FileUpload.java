package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-21:53
 * @Description:
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_uploads")
public class FileUpload extends BaseEntity {

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名
     */
    private String storedName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * 文件分类：IMAGE, DOCUMENT, VIDEO, AUDIO, OTHER
     */
    private String category;

    /**
     * 存储类型：LOCAL, MINIO, OSS
     */
    private String storageType;

    /**
     * 文件MD5值（用于去重）
     */
    private String md5;

    /**
     * 关联实体类型（post, comment, user等）
     */
    private String relatedType;

    /**
     * 关联实体ID
     */
    private Long relatedId;

    /**
     * 状态：1=正常，0=待确认，-1=已删除
     */
    private Integer status;
}