package com.dufeng.module.user.service;

import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.config.AuthProperties;
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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 短信验证码防暴破单测：错误次数超限作废验证码、发送日限额。
 */
class SmsCodeServiceTest {

    private static final String PHONE = "13800001234";
    private static final String CODE_KEY = "auth:sms:code:" + PHONE;
    private static final String FAIL_KEY = "auth:sms:fail:" + PHONE;
    private static final String DAILY_KEY = "auth:sms:daily:" + PHONE;

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private SmsCodeService smsCodeService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        smsCodeService = new SmsCodeService(redisTemplate, new AuthProperties());
    }

    @Test
    @DisplayName("验证码错误累计达上限后作废验证码")
    void tooManyFailuresInvalidatesCode() {
        when(valueOperations.get(CODE_KEY)).thenReturn("123456");
        when(valueOperations.increment(FAIL_KEY)).thenReturn(5L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> smsCodeService.verifyCode(PHONE, "000000"));

        assertEquals(ResultCode.VERIFY_CODE_ERROR.getCode(), exception.getCode());
        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(FAIL_KEY);
    }

    @Test
    @DisplayName("验证码正确时清除验证码与失败计数")
    void correctCodeClearsState() {
        when(valueOperations.get(CODE_KEY)).thenReturn("123456");

        assertDoesNotThrow(() -> smsCodeService.verifyCode(PHONE, "123456"));

        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(FAIL_KEY);
    }

    @Test
    @DisplayName("单手机号当日发送超限时拒绝")
    void dailySendLimitEnforced() {
        when(valueOperations.increment(contains(":daily"))).thenReturn(11L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> smsCodeService.sendCode(PHONE));

        assertEquals(ResultCode.SMS_DAILY_LIMIT.getCode(), exception.getCode());
        verify(valueOperations, never()).set(eq(CODE_KEY), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("正常发送时写入验证码并设置发送间隔")
    void normalSendStoresCode() {
        when(valueOperations.increment(any(String.class))).thenReturn(1L);
        when(redisTemplate.hasKey(any(String.class))).thenReturn(false);

        assertDoesNotThrow(() -> smsCodeService.sendCode(PHONE));

        verify(valueOperations).set(eq(CODE_KEY), anyString(), any(Duration.class));
        verify(valueOperations).set(eq("auth:sms:interval:" + PHONE), eq("1"), any(Duration.class));
    }
}
