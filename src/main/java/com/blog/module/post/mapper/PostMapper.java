package com.blog.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Category;
import com.blog.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-08-13:26
 * @Description:
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("UPDATE posts SET view_count = view_count + 1 WHERE id = #{postId}")
    void incrementViewCount(Long id);

    @Select("SELECT c.* FROM categories c " +
            "INNER JOIN post_categories pc ON c.id = pc.category_id " +
            "WHERE pc.post_id = #{postId}")
    List<Category> selectCategoriesByPostId(@Param("postId") Long postId);

}
