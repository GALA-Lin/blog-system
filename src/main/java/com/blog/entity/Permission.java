package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-18:21
 * @Description: 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permissions")
public class Permission extends BaseEntity {

    private String name;

    private String code; // e.g., post:create

    private String resource;

    private String action;

    private String description;
}
