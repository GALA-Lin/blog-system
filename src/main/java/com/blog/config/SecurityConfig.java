package com.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {



    /**
     * 安全过滤链（核心修改：放开所有请求）
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（保持不变）
                .csrf(AbstractHttpConfigurer::disable)

                // 保持无状态会话（保持不变）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 暂时注释异常处理（可选，避免未认证时的异常拦截）
                // .exceptionHandling(exception -> exception
                //         .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                //         .accessDeniedHandler(jwtAccessDeniedHandler))

                // 核心修改：允许所有请求访问
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 所有请求都无需认证
                );

        // 暂时移除JWT相关过滤器（关键：避免未配置好的JWT逻辑干扰）
        // .authenticationProvider(authenticationProvider())
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}