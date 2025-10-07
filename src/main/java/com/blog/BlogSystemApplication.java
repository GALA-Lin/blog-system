package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.blog.**.mapper")
@EnableCaching
@EnableAsync
@EnableScheduling
public class BlogSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogSystemApplication.class, args);
        System.out.println("""
            
            ==========================================
            Blog System Started Successfully!
            Swagger UI: http://localhost:8080/api/doc.html
            Druid Monitor: http://localhost:8080/api/druid/
            ==========================================
            """);
    }
}