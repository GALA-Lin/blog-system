package com.blog.VO.auth;

import lombok.Data;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-09-16:25
 * @Description: 用户简单数据传输对象，用于嵌套对象
 */
@Data
public class UserSimpleDTO {

    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;

}