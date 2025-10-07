package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-13:37
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_roles")
public class UserRole extends BaseEntity {

    private Long userId;

    private Long roleId;
}
