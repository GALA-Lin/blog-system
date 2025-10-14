package com.blog.VO.favorite;

import com.blog.VO.auth.UserSimpleDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-01:24
 * @Description:
 */
@Data
public class FolderVO {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Integer isPublic;
    private Integer isDefault;
    private Integer postCount;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 用户信息
     */
    private UserSimpleDTO user;
}