package com.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-17:23
 * @Description:
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * TODO: Cors configuration
     */

    /**
     * Static Resource Handler
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // Static files
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
