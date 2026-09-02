package com.dufeng.security;

/**
 * 基于 Redis 的登录会话与黑名单服务。
 * <p>登录会话键：auth:sess:{userId}；登出黑名单键：auth:blacklist:{jti}。</p>
 */
public interface AuthSessionService {

    void saveSession(Long userId, String token);

    boolean isInvalidSession(Long userId, String token);

    void blacklist(String jti, long ttlSeconds);

    boolean isBlacklisted(String jti);

    void logout(Long userId, String jti, long ttlSeconds);

    void invalidateUser(Long userId);
}
