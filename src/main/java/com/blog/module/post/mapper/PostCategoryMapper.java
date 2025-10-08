package com.blog.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Category;
import com.blog.entity.PostCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-17:23
 * @Description:
 */
@Mapper
public interface PostCategoryMapper extends BaseMapper<PostCategory> {



}
