package com.blog.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-14:41
 * @Description: 分页结果封装
 */
@Data
public class PageResult<T> {
    private Long total;
    private Long size;
    private Long current;
    private Long pages;
    private List<T> records;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords());
        return result;
    }

    public static <T> PageResult<T> of(Long total, Long size, Long current, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setSize(size);
        result.setCurrent(current);
        result.setPages((total + size - 1) / size);
        result.setRecords(records);
        return result;
    }
}
