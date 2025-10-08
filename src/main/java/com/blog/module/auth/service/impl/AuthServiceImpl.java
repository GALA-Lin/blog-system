package com.blog.module.auth.service.impl;

import com.blog.DTO.auth.LoginDTO;
import com.blog.DTO.auth.RegisterDTO;
import com.blog.VO.auth.LoginVO;
import com.blog.module.auth.mapper.RoleMapper;
import com.blog.module.auth.mapper.UserMapper;
import com.blog.module.auth.mapper.UserRoleMapper;
import com.blog.module.auth.service.AuthService;
import com.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.blog.common.ResultCode;
import com.blog.constants.SystemConstants;
import com.blog.entity.Role;
import com.blog.entity.User;
import com.blog.entity.UserRole;
import com.blog.common.BusinessException;
import com.blog.security.SecurityUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-07-16:55
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate; //redis模板

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 检查登录尝试
        checkLoginAttempts(loginDTO.getUsername());

        try {
            // 使用Spring Security进行登录
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );
            // 认证成功后重置登录尝试次数
            resetLoginAttempts(loginDTO.getUsername());

            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

            String token = jwtUtil.generateToken(securityUser);

            User user = new User();
            user.setId(securityUser.getUserId());
            user.setLastLoginAt(LocalDateTime.now());
            userMapper.updateById(user);

            List<String> roles = userMapper.selectRolesByUserId(securityUser.getUserId());
            List<String> permissions = userMapper.selectPermissionsByUserId(securityUser.getUserId());

            LocalDateTime expiresAt = LocalDateTime.now()
                    .plusSeconds(jwtUtil.extractExpiration(token).getTime() / 1000);

            return LoginVO.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresAt(expiresAt)
                    .userInfo(LoginVO.UserInfo.builder()
                            .id(securityUser.getUserId())
                            .username(securityUser.getUsername())
                            .email(securityUser.getEmail())
                            .nickname(securityUser.getNickname())
                            .avatarUrl(securityUser.getAvatarUrl())
                            .roles(roles)
                            .permissions(permissions)
                            .build())
                    .build();

        } catch (Exception e) {
            // 认证失败，增加登录尝试次数
            incrementLoginAttempts(loginDTO.getUsername());
            log.error("用户登录失败: {}", loginDTO.getUsername(), e);
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);
        }
    }

    /**
     * 检查登录尝试
     */
    private void checkLoginAttempts(String username) {
        String lockKey = String.format(SystemConstants.KEY_LOGIN_LOCKED, username);
        Boolean isLocked = redisTemplate.hasKey(lockKey);

        if (isLocked) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "由于多次尝试失败，您的账号已被锁定，请在" + ttl + "分钟后重试");
        }

        String attemptsKey = String.format(SystemConstants.KEY_LOGIN_ATTEMPTS, username);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);

        if (attempts != null && attempts >= 5) {
            // Lock account for 30 minutes
            redisTemplate.opsForValue().set(lockKey, true, 30, TimeUnit.MINUTES);
            redisTemplate.delete(attemptsKey);
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "由于多次尝试失败，您的账号已被锁定，请在30分钟后重试");
        }
    }

    /**
     * 增加登录尝试次数
     */
    private void incrementLoginAttempts(String username) {
        String key = String.format(SystemConstants.KEY_LOGIN_ATTEMPTS, username);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 15, TimeUnit.MINUTES);
    }

    /**
     * 重置登录尝试次数
     */
    private void resetLoginAttempts(String username) {
        String attemptsKey = String.format(SystemConstants.KEY_LOGIN_ATTEMPTS, username);
        String lockKey = String.format(SystemConstants.KEY_LOGIN_LOCKED, username);
        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(lockKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        if(userMapper.existsByUsername(registerDTO.getUsername())){
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS,"Username already exists");
        }
        // 检查邮箱是否存在
        if(userMapper.existsByEmail(registerDTO.getEmail())){
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS,"Email already exists");
        }
        // 保存用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setNickname(registerDTO.getNickname() != null ?
                registerDTO.getNickname() : registerDTO.getUsername());

        // 密码加密
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        user.setStatus(SystemConstants.USER_STATUS_ACTIVE);
        user.setEmailVerified(0);
        user.setPostCount(0);
        user.setFollowerCount(0);
        user.setFollowingCount(0);

        userMapper.insert(user);

        // 分配默认角色
        assignDefaultRole(user.getId());

        log.info("用户注册成功: {}", user.getUsername());
    }
    private void assignDefaultRole(Long userId) {
        // 设置默认角色
        Role readerRole = roleMapper.selectByCode("ROLE_READER");
        if (readerRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(readerRole.getId());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public void logout(String token) {
        // Add token to blacklist (optional)
        String username = jwtUtil.extractUsername(token);
        Long expiration = jwtUtil.extractExpiration(token).getTime();
        Long now = System.currentTimeMillis();

        if (expiration > now) {
            String key = "token:blacklist:" + token;
            redisTemplate.opsForValue().set(key, username,
                    expiration - now, TimeUnit.MILLISECONDS);
        }

        log.info("用户登出成功: {}", username);
    }
}

