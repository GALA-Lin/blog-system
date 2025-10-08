package com.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-17:24
 * @Description: API文档配置
 */
@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("博客系统API文档")
                        .version("1.0.0")
                        .description("Modern Blog System built with Spring Boot 3.2.0")
                        .contact(new Contact()
                                .name("GALA_Lin")
                                .email("gala_lin@outlook.com")
                                .url("https://github.com/GALA-Lin"))
                        .license(new License()
                                .name("GPL-3.0")
                                .url("https://www.gnu.org/licenses/gpl-3.0.en.html")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}
