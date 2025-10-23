package com.blog.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-21:57
 * @Description:
 */
@Mapper
public interface FileUploadMapper extends BaseMapper<FileUpload> {

    /**
     * 根据MD5查询文件
     */
    @Select("SELECT * FROM file_uploads WHERE md5 = #{md5} AND status = 1 LIMIT 1")
    FileUpload selectByMd5(@Param("md5") String md5);

    /**
     * 查询用户上传的文件列表
     */
    @Select("SELECT * FROM file_uploads " +
            "WHERE user_id = #{userId} " +
            "AND status = 1 " +
            "ORDER BY created_at DESC")
    IPage<FileUpload> selectUserFiles(Page<?> page, @Param("userId") Long userId);

    /**
     * 根据分类查询文件
     */
    @Select("SELECT * FROM file_uploads " +
            "WHERE user_id = #{userId} " +
            "AND category = #{category} " +
            "AND status = 1 " +
            "ORDER BY created_at DESC")
    IPage<FileUpload> selectFilesByCategory(
            Page<?> page,
            @Param("userId") Long userId,
            @Param("category") String category
    );

    /**
     * 统计用户上传文件总大小
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM file_uploads " +
            "WHERE user_id = #{userId} AND status = 1")
    Long calculateTotalSize(@Param("userId") Long userId);

    /**
     * 统计用户文件数量
     */
    @Select("SELECT COUNT(*) FROM file_uploads " +
            "WHERE user_id = #{userId} AND status = 1")
    Long countUserFiles(@Param("userId") Long userId);

    /**
     * 更新文件关联信息
     */
    @Update("UPDATE file_uploads " +
            "SET related_type = #{relatedType}, related_id = #{relatedId} " +
            "WHERE id = #{fileId}")
    int updateRelatedInfo(
            @Param("fileId") Long fileId,
            @Param("relatedType") String relatedType,
            @Param("relatedId") Long relatedId
    );

    /**
     * 批量删除文件（软删除）
     */
    @Update({
            "<script>",
            "UPDATE file_uploads SET status = -1 ",
            "WHERE id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "AND user_id = #{userId}",
            "</script>"
    })
    int batchDelete(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}