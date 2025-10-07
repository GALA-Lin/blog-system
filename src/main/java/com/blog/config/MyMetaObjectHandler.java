package com.blog.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.blog.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-14:36
 * @Description: 自定义mybatis-plus填充器-自动填充创建时间和更新时间
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充....");
        Object originalObject = metaObject.getOriginalObject();
        // 排除UserRole实体，不填充updatedAt
        if (!(originalObject instanceof UserRole)) {
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
        // 所有实体都填充createdAt
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充....");
        Object originalObject = metaObject.getOriginalObject();
        // 排除UserRole实体，不填充updatedAt
        if (!(originalObject instanceof UserRole)) {
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
