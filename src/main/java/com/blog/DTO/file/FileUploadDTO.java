package com.blog.DTO.file;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-21:56
 * @Description:
 */
@Data
@Builder
public class FileUploadDTO {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名
     */
    private String storedName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化）
     */
    private String fileSizeFormatted;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * 文件分类
     */
    private String category;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 缩略图URL（仅图片）
     */
    private String thumbnailUrl;
}
