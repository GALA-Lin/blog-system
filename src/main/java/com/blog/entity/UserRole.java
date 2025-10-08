package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-13:37
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_roles")
public class UserRole extends BaseEntity {

    // 覆盖父类的updatedAt字段，声明其在数据库中不存在
    @TableField(exist = false)
    private LocalDateTime updatedAt;

    private Long userId;

    private Long roleId;
}
