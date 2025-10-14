package com.blog.module.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.FavoriteFolder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-14-11:36
 * @Description:
 */
public interface FavoriteFolderMapper extends BaseMapper<FavoriteFolder> {

    /**
     * 获取用户收藏夹列表
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    @Select("SELECT * FROM favorite_folders " +
            "WHERE user_id = #{userId} " +
            "ORDER BY is_default DESC, sort_order ASC, created_at DESC")
    List<FavoriteFolder> selectUserFolders(@Param("userId") Long userId);

    /**
     * 获取默认收藏夹
     * @param userId 用户ID
     * @return
     */
    @Select("SELECT * FROM favorite_folders WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    FavoriteFolder selectDefaultFolder(@Param("userId") Long userId);

    /**
     * 判断收藏夹名称是否存在
     * @param userId 用户ID
     * @param name 收藏夹名称
     * @param excludeId 排除的收藏夹ID
     * @return true：存在；false：不存在
     */
    @Select("SELECT COUNT(*) > 0 FROM favorite_folders WHERE user_id = #{userId} AND name = #{name} AND id != #{excludeId}")
    Boolean existsByName(@Param("userId") Long userId, @Param("name") String name, @Param("excludeId") Long excludeId);


}
