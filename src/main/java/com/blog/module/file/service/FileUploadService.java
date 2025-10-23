package com.blog.module.file.service;

import com.blog.DTO.file.FileUploadDTO;
import com.blog.common.PageResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-22:02
 * @Description:
 */
public interface FileUploadService {

    /**
     * 上传文件
     * @param file 文件
     * @param userId 用户ID
     * @param category 文件分类
     * @return 上传成功的文件信息
     */
    FileUploadDTO uploadFile(MultipartFile file, Long userId, String category);

    /**
     * 上传图片
     * @param file 文件
     * @param userId 用户ID
     * @return 文件信息
     */
    FileUploadDTO uploadImage(MultipartFile file, Long userId);

    /**
     * 批量上传文件
     * @param files 文件数组
     * @param userId 用户ID
     * @param category 文件分类
     * @return 上传成功的文件信息列表
     */
    List<FileUploadDTO> batchUpload(MultipartFile[] files, Long userId, String category);

    /**
     * 删除文件
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteFile(Long fileId, Long userId);

    /**
     * 批量删除
     * @param fileIds 文件ID列表
     * @param userId 用户ID
     * @return 删除数
     */
    Integer batchDelete(List<Long> fileIds, Long userId);

    /**
     * 获取用户文件列表
     * @param userId 用户ID
     * @param category 文件分类（可选）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 文件列表
     */
    PageResult<FileUploadDTO> getUserFiles(Long userId, String category, Integer pageNum, Integer pageSize);

    /**
     * 获取文件详情
     * @param fileId 文件ID
     * @return 文件信息
     */
    FileUploadDTO getFileDetail(Long fileId);

    /**
     * 更新文件关联信息
     * @param fileId 文件ID
     * @param relatedType 关联类型
     * @param relatedId 关联ID
     * @param userId 用户ID
     */
    void updateRelatedInfo(Long fileId, String relatedType, Long relatedId, Long userId);

    /**
     * 统计用户文件总大小
     * @param userId 用户ID
     * @return 总大小（字节）
     */
    Long calculateUserTotalSize(Long userId);

    /**
     * 统计用户文件数量
     * @param userId 用户ID
     * @return 文件数量
     */
    Long countUserFiles(Long userId);


}
