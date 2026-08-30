package com.dufeng.common.constant;

/**
 * Redis 键前缀统一管理。
 */
public final class RedisKeyConstants {

    public static final String AUTH_SESSION = "auth:sess:";
    public static final String AUTH_BLACKLIST = "auth:blacklist:";
    public static final String AUTH_SMS_CODE = "auth:sms:code:";
    public static final String AUTH_SMS_INTERVAL = "auth:sms:interval:";
    public static final String AUTH_SMS_FAIL = "auth:sms:fail:";
    public static final String AUTH_SMS_DAILY = "auth:sms:daily:";
    public static final String AUTH_LOGIN_FAIL = "auth:login:fail:";
    public static final String ORDER_IDEMPOTENT = "order:idem:";

    private RedisKeyConstants() {
    }
}
