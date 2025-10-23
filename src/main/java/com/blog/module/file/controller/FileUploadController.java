package com.blog.module.file.controller;

import com.blog.DTO.file.FileUploadDTO;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.module.file.service.FileUploadService;
import com.blog.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author: GALA_Lin
 * @Date: 2025-10-23-09:44
 * @Description:
 */
@Tag(name = "文件上传", description = "文件上传相关API")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<FileUploadDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String category) {
        Long userId = SecurityUtil.getCurrentUserId();
        FileUploadDTO result = fileUploadService.uploadFile(file, userId, category);
        return Result.success("文件上传成功", result);
    }


    @Operation(summary = "上传图片")
    @PostMapping("/upload/image")
    @PreAuthorize("isAuthenticated()")
    public Result<FileUploadDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        FileUploadDTO result = fileUploadService.uploadImage(file, userId);
        return Result.success("图片上传成功", result);
    }

    @Operation(summary = "批量上传文件")
    @PostMapping("/upload/batch")
    @PreAuthorize("isAuthenticated()")
    public Result<List<FileUploadDTO>> batchUpload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) String category) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<FileUploadDTO> results = fileUploadService.batchUpload(files, userId, category);
        return Result.success("批量上传完成", results);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> deleteFile(@PathVariable Long fileId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Boolean result = fileUploadService.deleteFile(fileId, userId);
        return Result.success(result ? "文件删除成功" : "文件删除失败", result);
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public Result<Integer> batchDelete(@RequestBody List<Long> fileIds) {
        Long userId = SecurityUtil.getCurrentUserId();
        Integer count = fileUploadService.batchDelete(fileIds, userId);
        return Result.success("成功删除 " + count + " 个文件", count);
    }

    @Operation(summary = "获取我的文件列表")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<FileUploadDTO>> getMyFiles(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        PageResult<FileUploadDTO> files = fileUploadService.getUserFiles(userId, category, pageNum, pageSize);
        return Result.success(files);
    }

    @Operation(summary = "获取文件详情")
    @GetMapping("/{fileId}")
    public Result<FileUploadDTO> getFileDetail(@PathVariable Long fileId) {
        FileUploadDTO file = fileUploadService.getFileDetail(fileId);
        return Result.success(file);
    }

    @Operation(summary = "更新文件关联信息")
    @PutMapping("/{fileId}/relate")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateRelatedInfo(
            @PathVariable Long fileId,
            @RequestParam String relatedType,
            @RequestParam Long relatedId) {
        Long userId = SecurityUtil.getCurrentUserId();
        fileUploadService.updateRelatedInfo(fileId, relatedType, relatedId, userId);
        return Result.success("关联信息更新成功");
    }

    @Operation(summary = "获取我的存储统计")
    @GetMapping("/my/stats")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getMyStats() {
        Long userId = SecurityUtil.getCurrentUserId();

        Long totalSize = fileUploadService.calculateUserTotalSize(userId);
        Long fileCount = fileUploadService.countUserFiles(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSize", totalSize);
        stats.put("totalSizeFormatted", formatFileSize(totalSize));
        stats.put("fileCount", fileCount);
        stats.put("maxSize", 10 * 1024 * 1024 * 1024L); // 10GB限制
        stats.put("maxSizeFormatted", "10 GB");
        stats.put("usagePercent", totalSize * 100.0 / (10 * 1024 * 1024 * 1024L));

        return Result.success(stats);
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return String.format("%.1f %s",
                size / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }
}
