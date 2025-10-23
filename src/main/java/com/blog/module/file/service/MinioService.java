package com.blog.module.file.service;

import com.blog.common.BusinessException;
import com.blog.config.properties.MinioProperties;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import java.util.concurrent.TimeUnit;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-22:15
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * 检查存储桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        } catch (Exception e) {
            log.error("检查存储桶失败: {}", bucketName, e);
            return false;
        }
    }

    /**
     * 创建存储桶
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("MinIO存储桶创建成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", bucketName, e);
            throw new BusinessException("创建存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到MinIO
     */
    public void uploadFile(String bucketName, String objectName, InputStream inputStream,
                           long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("文件上传到MinIO成功: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("文件上传到MinIO失败: {}/{}", bucketName, objectName, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传MultipartFile到MinIO
     */
    public void uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            uploadFile(bucketName, objectName, file.getInputStream(),
                    file.getSize(), file.getContentType());
        } catch (IOException e) {
            log.error("读取文件流失败", e);
            throw new BusinessException("读取文件流失败: " + e.getMessage());
        }
    }

    /**
     * 从MinIO下载文件
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("从MinIO下载文件失败: {}/{}", bucketName, objectName, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除MinIO中的文件
     */
    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("从MinIO删除文件成功: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("从MinIO删除文件失败: {}/{}", bucketName, objectName, e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件的访问URL（临时URL）
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expires 过期时间（秒）
     */
    public String getPresignedObjectUrl(String bucketName, String objectName, int expires) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expires, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}/{}", bucketName, objectName, e);
            throw new BusinessException("获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件的永久访问URL
     */
    public String getObjectUrl(String bucketName, String objectName) {
        String endpoint = minioProperties.getPublicEndpoint() != null ?
                minioProperties.getPublicEndpoint() : minioProperties.getEndpoint();
        return String.format("%s/%s/%s", endpoint, bucketName, objectName);
    }

    /**
     * 检查文件是否存在
     */
    public boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public ObjectStat getObjectInfo(String bucketName, String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            return ObjectStat.builder()
                    .size(stat.size())
                    .contentType(stat.contentType())
                    .etag(stat.etag())
                    .lastModified(stat.lastModified())
                    .build();
        } catch (Exception e) {
            log.error("获取文件信息失败: {}/{}", bucketName, objectName, e);
            throw new BusinessException("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 复制文件
     */
    public void copyObject(String sourceBucket, String sourceObject,
                           String destBucket, String destObject) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(sourceBucket)
                                    .object(sourceObject)
                                    .build())
                            .bucket(destBucket)
                            .object(destObject)
                            .build()
            );
            log.info("MinIO文件复制成功: {}/{} -> {}/{}",
                    sourceBucket, sourceObject, destBucket, destObject);
        } catch (Exception e) {
            log.error("MinIO文件复制失败", e);
            throw new BusinessException("文件复制失败: " + e.getMessage());
        }
    }

    /**
     * 设置存储桶为公开访问
     */
    public void setBucketPublic(String bucketName) {
        try {
            String policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": "*"},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """.formatted(bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("存储桶设置为公开访问: {}", bucketName);
        } catch (Exception e) {
            log.error("设置存储桶策略失败: {}", bucketName, e);
            throw new BusinessException("设置存储桶策略失败: " + e.getMessage());
        }
    }

    /**
     * 文件信息统计类
     */
    @lombok.Data
    @lombok.Builder
    public static class ObjectStat {
        private long size;
        private String contentType;
        private String etag;
        private java.time.ZonedDateTime lastModified;
    }
}
