package com.dufeng.security;

import com.dufeng.common.constant.RedisKeyConstants;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录防爆破：同一账号连续失败达到上限后锁定一段时间。
 * Redis 不可用时降级放行，不阻断正常登录。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginGuard {

    private static final int MAX_FAILS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;

    public void checkLocked(String account) {
        try {
            String fails = redisTemplate.opsForValue().get(key(account));
            if (fails != null && Integer.parseInt(fails) >= MAX_FAILS) {
                throw new BusinessException(ResultCode.LOGIN_LOCKED);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[Redis] 登录锁定检查失败，降级放行：{}", e.getMessage());
        }
    }

    public void recordFailure(String account) {
        try {
            String k = key(account);
            Long fails = redisTemplate.opsForValue().increment(k);
            if (fails != null && fails == 1) {
                redisTemplate.expire(k, LOCK_DURATION);
            }
        } catch (Exception e) {
            log.warn("[Redis] 记录登录失败次数异常：{}", e.getMessage());
        }
    }

    public void reset(String account) {
        try {
            redisTemplate.delete(key(account));
        } catch (Exception e) {
            log.warn("[Redis] 清除登录失败次数异常：{}", e.getMessage());
        }
    }

    private String key(String account) {
        return RedisKeyConstants.AUTH_LOGIN_FAIL + account;
    }
}
