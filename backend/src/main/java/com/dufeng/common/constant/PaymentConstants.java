package com.dufeng.common.constant;

/**
 * 支付相关常量统一管理。签名密钥从配置读取（dufeng.pay.secret），禁止硬编码。
 */
public final class PaymentConstants {

    public static final String CHANNEL_ALIPAY = "alipay";
    public static final String CHANNEL_WECHAT = "wechat";
    public static final String CHANNEL_BALANCE = "balance";

    public static final String PAY_NO_PREFIX = "PAY";
    public static final String PREPAY_PARAMS_PREFIX = "mock-prepay:";
    public static final String SIGN_FIELD_SEPARATOR = "|";
    public static final String SIGN_ALGORITHM = "HmacSHA256";

    private PaymentConstants() {
    }
}
