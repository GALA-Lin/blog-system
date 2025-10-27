package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-18:18
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("posts")
public class Post extends BaseEntity {

    private Long userId;

    private String title;

    private String slug;

    private String summary;

    @TableField(value = "`content`")
    private String content;

    private String contentType; // MARKDOWN, HTML, RICHTEXT

    private String coverImage;

    private Integer status; // 0=draft, 1=published, 2=under_review, -1=deleted

    private Integer isTop; // 0=normal, 1=pinned

    private Integer isOriginal; // 1=original, 0=reprinted

    private String originalUrl;

    private Long viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer commentCount;

    private Integer allowComment; // 1=allow, 0=disable

    private LocalDateTime publishedAt;
}