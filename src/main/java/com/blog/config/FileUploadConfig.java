package com.blog.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.MultipartConfigElement;
/**
 * @Author: GALA_Lin
 * @Date: 2025-10-23-09:42
 * @Description:
 */
@Configuration
public class FileUploadConfig {

    /**
     * 配置文件上传大小限制
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // 单个文件最大 10MB
        factory.setMaxFileSize(DataSize.ofMegabytes(10));

        // 总上传大小最大 20MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(20));

        return factory.createMultipartConfig();
    }

    /**
     * CORS配置（用于文件上传）
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源（生产环境应该限制）
        config.addAllowedOriginPattern("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有HTTP方法
        config.addAllowedMethod("*");

        // 允许携带凭证
        config.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}