package com.dufeng.module.user.service;

import com.dufeng.common.constant.RedisKeyConstants;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 短信验证码服务。生产环境可对接阿里云/腾讯云短信，这里以 Redis 模拟并记录日志。
 * <p>安全约束：验证码 5 分钟内最多错 5 次即作废；单手机号每日限量；日志不落验证码明文。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private static final int MAX_VERIFY_FAILS = 5;
    private static final int DAILY_SEND_LIMIT = 10;

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public void sendCode(String phone) {
        try {
            String intervalKey = RedisKeyConstants.AUTH_SMS_INTERVAL + phone;
            Boolean hasSent = redisTemplate.hasKey(intervalKey);
            if (Boolean.TRUE.equals(hasSent)) {
                throw new BusinessException(ResultCode.SMS_TOO_FREQUENT);
            }
            String dailyKey = RedisKeyConstants.AUTH_SMS_DAILY + phone + ":"
                    + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            Long sentToday = redisTemplate.opsForValue().increment(dailyKey);
            if (sentToday != null && sentToday == 1) {
                redisTemplate.expire(dailyKey, Duration.ofHours(24));
            }
            if (sentToday != null && sentToday > DAILY_SEND_LIMIT) {
                throw new BusinessException(ResultCode.SMS_DAILY_LIMIT);
            }
            String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
            redisTemplate.opsForValue().set(
                    RedisKeyConstants.AUTH_SMS_CODE + phone,
                    code,
                    Duration.ofSeconds(authProperties.getSmsExpireSeconds()));
            redisTemplate.opsForValue().set(
                    intervalKey,
                    "1",
                    Duration.ofSeconds(authProperties.getSmsSendIntervalSeconds()));
            // 只记录脱敏手机号，不记录验证码明文
            log.info("[短信模拟] 验证码已发送至 {}", mask(phone));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[Redis] 发送验证码失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
    }

    public void verifyCode(String phone, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
        try {
            String codeKey = RedisKeyConstants.AUTH_SMS_CODE + phone;
            String failKey = RedisKeyConstants.AUTH_SMS_FAIL + phone;
            String saved = redisTemplate.opsForValue().get(codeKey);
            if (saved == null) {
                throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
            }
            if (!saved.equals(code)) {
                Long fails = redisTemplate.opsForValue().increment(failKey);
                if (fails != null && fails == 1) {
                    redisTemplate.expire(failKey, Duration.ofSeconds(authProperties.getSmsExpireSeconds()));
                }
                if (fails != null && fails >= MAX_VERIFY_FAILS) {
                    // 错误次数超限，验证码立即作废，需重新获取
                    redisTemplate.delete(codeKey);
                    redisTemplate.delete(failKey);
                }
                throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
            }
            redisTemplate.delete(codeKey);
            redisTemplate.delete(failKey);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[Redis] 校验验证码失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
    }

    private String mask(String phone) {
        return phone.length() == 11
                ? phone.substring(0, 3) + "****" + phone.substring(7)
                : phone;
    }
}
