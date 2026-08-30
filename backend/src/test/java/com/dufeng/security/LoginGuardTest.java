package com.dufeng.security;

import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录防爆破单测：连续失败达到上限后锁定，成功后清零。
 */
class LoginGuardTest {

    private static final String ACCOUNT = "admin";
    private static final String KEY = "auth:login:fail:" + ACCOUNT;

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private LoginGuard loginGuard;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        loginGuard = new LoginGuard(redisTemplate);
    }

    @Test
    @DisplayName("失败次数达到上限后锁定登录")
    void lockedAfterMaxFailures() {
        when(valueOperations.get(KEY)).thenReturn("5");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> loginGuard.checkLocked(ACCOUNT));

        assertEquals(ResultCode.LOGIN_LOCKED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("失败次数未达上限时放行")
    void allowsBelowThreshold() {
        when(valueOperations.get(KEY)).thenReturn("4");

        assertDoesNotThrow(() -> loginGuard.checkLocked(ACCOUNT));
    }

    @Test
    @DisplayName("记录失败时首次写入设置 15 分钟过期")
    void recordFailureSetsExpiryOnce() {
        when(valueOperations.increment(KEY)).thenReturn(1L);

        loginGuard.recordFailure(ACCOUNT);

        verify(redisTemplate).expire(KEY, Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("登录成功后清除失败计数")
    void resetClearsCounter() {
        loginGuard.reset(ACCOUNT);

        verify(redisTemplate).delete(KEY);
    }

    @Test
    @DisplayName("Redis 异常时降级放行，不阻断登录")
    void redisFailureFailsOpen() {
        when(valueOperations.get(anyString())).thenThrow(new IllegalStateException("redis down"));

        assertDoesNotThrow(() -> loginGuard.checkLocked(ACCOUNT));
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }
}
