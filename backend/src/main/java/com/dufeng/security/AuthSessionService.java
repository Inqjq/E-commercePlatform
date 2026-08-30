package com.dufeng.security;

import com.dufeng.config.AuthProperties;
import com.dufeng.common.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 基于 Redis 的登录会话与黑名单服务。
 * <p>登录会话键：auth:sess:{userId}；登出黑名单键：auth:blacklist:{jti}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public void saveSession(Long userId, String token) {
        try {
            redisTemplate.opsForValue().set(
                    RedisKeyConstants.AUTH_SESSION + userId,
                    token,
                    Duration.ofSeconds(authProperties.getLoginExpireSeconds()));
        } catch (Exception e) {
            log.warn("[Redis] 保存登录会话失败：{}", e.getMessage());
        }
    }

    /**
     * 校验令牌是否为当前用户最新会话（旧会话应已过期或被新会话覆盖）。
     */
    public boolean isInvalidSession(Long userId, String token) {
        try {
            String current = redisTemplate.opsForValue().get(RedisKeyConstants.AUTH_SESSION + userId);
            if (current == null) {
                return true;
            }
            return !current.equals(token);
        } catch (Exception e) {
            log.warn("[Redis] 查询登录会话失败：{}", e.getMessage());
            return false;
        }
    }

    public void blacklist(String jti, long ttlSeconds) {
        if (jti == null) {
            return;
        }
        try {
            if (ttlSeconds <= 0) {
                ttlSeconds = authProperties.getLoginExpireSeconds();
            }
            redisTemplate.opsForValue().set(
                    RedisKeyConstants.AUTH_BLACKLIST + jti,
                    String.valueOf(jti),
                    Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("[Redis] 写入黑名单失败：{}", e.getMessage());
        }
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyConstants.AUTH_BLACKLIST + jti));
        } catch (Exception e) {
            log.warn("[Redis] 查询黑名单失败：{}", e.getMessage());
            return false;
        }
    }

    public void logout(Long userId, String jti, long ttlSeconds) {
        try {
            redisTemplate.delete(RedisKeyConstants.AUTH_SESSION + userId);
        } catch (Exception e) {
            log.warn("[Redis] 删除登录会话失败：{}", e.getMessage());
        }
        blacklist(jti, ttlSeconds);
    }

    /**
     * 失效该用户的存量会话（改密、重置密码、禁用、角色变更时调用），
     * 用户下次请求将被强制重新登录。
     */
    public void invalidateUser(Long userId) {
        try {
            redisTemplate.delete(RedisKeyConstants.AUTH_SESSION + userId);
        } catch (Exception e) {
            log.warn("[Redis] 失效用户会话失败：{}", e.getMessage());
        }
    }
}
