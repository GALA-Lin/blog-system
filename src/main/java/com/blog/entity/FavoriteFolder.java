package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-12-16:24
 * @Description:
 */
@Data
@TableName(value = "favorite_folders")
public class FavoriteFolder  extends BaseEntity {


    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏夹名称
     */
    private String name;

    /**
     * 收藏夹描述
     */
    private String description;

    /**
     * 是否公开: 0=私密, 1=公开
     */
    private Integer isPublic;

    /**
     * 是否默认收藏夹: 0=否, 1=是
     */
    private Integer isDefault;

    /**
     * 收藏夹内文章数量
     */
    private Integer postCount;

    /**
     * 排序顺序
     */
    private Integer sortOrder;


    // ========== 瞬态字段 ==========

    /**
     * 用户信息（关联查询）
     */
    @TableField(exist = false)
    private User user;
}
