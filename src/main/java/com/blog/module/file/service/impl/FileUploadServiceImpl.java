package com.blog.module.file.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.DTO.file.FileUploadDTO;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.config.properties.MinioProperties;
import com.blog.entity.FileUpload;
import com.blog.module.file.mapper.FileUploadMapper;
import com.blog.module.file.service.FileUploadService;
import com.blog.module.file.service.MinioService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-22:06
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService{

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileUploadMapper fileUploadMapper;

    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // 允许的文档类型
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/octet-stream",
            "text/plain",
            "text/markdown"
    );

    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final MinioService minioService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadDTO uploadFile(MultipartFile file, Long userId, String category) {
        String contentType = file.getContentType();
        if ("IMAGE".equals(category)) {
            // 验证图片类型（如image/jpeg）
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new BusinessException("不支持的图片类型: " + contentType);
            }
        } else if ("DOCUMENT".equals(category)) {
            // 验证文档类型
            if (!ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
                throw new BusinessException("不支持的文件类型: " + contentType);
            }
        }
        // 1. 验证文件
        validateFile(file, category);

        try {
            // 2. 计算文件MD5（用于去重）
            String md5 = calculateMd5(file);

            // 3. 检查是否已存在相同文件（秒传功能）
            FileUpload existingFile = fileUploadMapper.selectByMd5(md5);
            if (existingFile != null) {
                log.info("【MinIO秒传】文件已存在，直接返回: {}", existingFile.getOriginalName());
                return convertToDTO(existingFile);
            }

            // 4. 确保MinIO存储桶存在
            String bucketName = minioProperties.getBucketName();
            if (!minioService.bucketExists(bucketName)) {
                minioService.createBucket(bucketName);
                // 设置存储桶为公开访问（可选）
                minioService.setBucketPublic(bucketName);
            }

            // 5. 生成文件存储信息
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String storedName = generateStoredName(extension);
            String filePath = generateFilePath(userId, category) + storedName;

            // 6. 上传文件到MinIO
            log.info("【MinIO上传】开始上传到MinIO: bucket={}, path={}", bucketName, filePath);
            minioService.uploadFile(bucketName, filePath, file);
            log.info("【MinIO上传】上传成功: {}", filePath);

            // 7. 生成访问URL
            String fileUrl = minioService.getObjectUrl(bucketName, filePath);
            log.info("【MinIO访问URL】{}", fileUrl);

            // 8. 保存文件记录到数据库
            FileUpload fileUpload = new FileUpload();
            fileUpload.setUserId(userId);
            fileUpload.setOriginalName(originalFilename);
            fileUpload.setStoredName(storedName);
            fileUpload.setFilePath(filePath);
            fileUpload.setFileUrl(fileUrl);
            fileUpload.setFileSize(file.getSize());
            fileUpload.setMimeType(file.getContentType());
            fileUpload.setExtension(extension);
            fileUpload.setCategory(category != null ? category : determineCategory(file.getContentType()));
            fileUpload.setStorageType("MINIO"); // 明确标记存储类型为MINIO
            fileUpload.setMd5(md5);
            fileUpload.setStatus(1);

            fileUploadMapper.insert(fileUpload);

            log.info("【数据库保存】文件记录保存成功: {} -> {}", originalFilename, filePath);
            return convertToDTO(fileUpload);

        } catch (Exception e) {
            log.error("【文件上传失败】", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadDTO uploadImage(MultipartFile file, Long userId) {
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BusinessException("只支持上传图片文件（JPG、PNG、GIF、WebP）");
        }
        return uploadFile(file, userId, "IMAGE");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileUploadDTO> batchUpload(MultipartFile[] files, Long userId, String category) {
        List<FileUploadDTO> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileUploadDTO dto = uploadFile(file, userId, category);
                results.add(dto);
            } catch (Exception e) {
                log.error("批量上传文件失败: {}", file.getOriginalFilename(), e);
            }
        }

        return results;
    }

    /**
     * 删除文件（软删除）
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFile(Long fileId, Long userId) {
        FileUpload file = fileUploadMapper.selectById(fileId);

        if (file == null) {
            throw new BusinessException("文件不存在");
        }

        if (!file.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此文件");
        }

        try {
            // 从MinIO删除
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(file.getFilePath())
                            .build()
            );

            // 软删除数据库记录
            file.setStatus(-1);
            fileUploadMapper.updateById(file);

            log.info("文件删除成功: {}", file.getFilePath());
            return true;

        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除（软删除）
     * @param fileIds 文件ID列表
     * @param userId 用户ID
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer batchDelete(List<Long> fileIds, Long userId) {
        return fileUploadMapper.batchDelete(fileIds, userId);
    }

    /**
     * 获取用户文件列表
     * @param userId 用户ID
     * @param category 文件分类（可选）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 文件列表
     */
    @Override
    public PageResult<FileUploadDTO> getUserFiles(Long userId, String category, Integer pageNum, Integer pageSize) {
        Page<FileUpload> page = new Page<>(pageNum, pageSize);

        IPage<FileUpload> filePage;
        if (StringUtils.hasText(category)) {
            filePage = fileUploadMapper.selectFilesByCategory(page, userId, category);
        } else {
            filePage = fileUploadMapper.selectUserFiles(page, userId);
        }

        List<FileUploadDTO> dtoList = filePage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();

        return new PageResult<>(dtoList, filePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public FileUploadDTO getFileDetail(Long fileId) {
        FileUpload file = fileUploadMapper.selectById(fileId);
        if (file == null || file.getStatus() == -1) {
            throw new BusinessException("文件不存在");
        }

        // 可以选择性地检查MinIO中文件是否存在
        String bucketName = minioProperties.getBucketName();
        boolean exists = minioService.objectExists(bucketName, file.getFilePath());
        log.debug("【MinIO检查】文件存在性: bucket={}, path={}, exists={}",
                bucketName, file.getFilePath(), exists);

        return convertToDTO(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRelatedInfo(Long fileId, String relatedType, Long relatedId, Long userId) {
        FileUpload file = fileUploadMapper.selectById(fileId);

        if (file == null) {
            throw new BusinessException("文件不存在");
        }

        if (!file.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此文件");
        }

        fileUploadMapper.updateRelatedInfo(fileId, relatedType, relatedId);
    }

    @Override
    public Long calculateUserTotalSize(Long userId) {
        return fileUploadMapper.calculateTotalSize(userId);
    }

    @Override
    public Long countUserFiles(Long userId) {
        return fileUploadMapper.countUserFiles(userId);
    }

    // ========== 辅助方法 ==========

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, String category) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 " + formatFileSize(MAX_FILE_SIZE));
        }

        String contentType = file.getContentType();
        if (!isAllowedType(contentType, category)) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedType(String contentType, String category) {
        if (category == null) {
            return ALLOWED_IMAGE_TYPES.contains(contentType) ||
                    ALLOWED_DOCUMENT_TYPES.contains(contentType);
        }

        return switch (category) {
            case "IMAGE" -> ALLOWED_IMAGE_TYPES.contains(contentType);
            case "DOCUMENT" -> ALLOWED_DOCUMENT_TYPES.contains(contentType);
            default -> false;
        };
    }

    /**
     * 计算文件MD5
     */
    private String calculateMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        } catch (Exception e) {
            log.error("计算文件MD5失败", e);
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 生成存储文件名
     */
    private String generateStoredName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") +
                (StringUtils.hasText(extension) ? "." + extension : "");
    }

    /**
     * 生成文件存储路径
     * 格式: {category}/{year}/{month}/{day}/{userId}/{filename}
     * 例如: image/2025/01/15/1/abc123.jpg
     */
    private String generateFilePath(Long userId, String category) {
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String categoryPath = category != null ? category.toLowerCase() : "other";
        return String.format("%s/%s/%d/", categoryPath, datePath, userId);
    }

    /**
     * 根据MIME类型确定文件分类
     */
    private String determineCategory(String mimeType) {
        if (mimeType == null) {
            return "OTHER";
        }

        if (mimeType.startsWith("image/")) {
            return "IMAGE";
        } else if (mimeType.startsWith("video/")) {
            return "VIDEO";
        } else if (mimeType.startsWith("audio/")) {
            return "AUDIO";
        } else if (ALLOWED_DOCUMENT_TYPES.contains(mimeType)) {
            return "DOCUMENT";
        }

        return "OTHER";
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 转换为DTO
     */
    private FileUploadDTO convertToDTO(FileUpload file) {
        return FileUploadDTO.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .storedName(file.getStoredName())
                .fileUrl(file.getFileUrl())
                .fileSize(file.getFileSize())
                .fileSizeFormatted(formatFileSize(file.getFileSize()))
                .mimeType(file.getMimeType())
                .extension(file.getExtension())
                .category(file.getCategory())
                .uploadTime(file.getCreatedAt())
                .build();
    }
}
