package com.blog.security;

import com.blog.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-15:16
 * @Description: 自定义UserDetails，实现Spring Security的UserDetailsService接口
 */
@Data
public class SecurityUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private Boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;

    // 添加用户信息
    private String email;
    private String nickname;
    private String avatarUrl;

    public SecurityUser(User user, List<String> permissions) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.getStatus() == 1;
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.avatarUrl = user.getAvatarUrl();

        // 切换权限到GrantedAuthority
        this.authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
