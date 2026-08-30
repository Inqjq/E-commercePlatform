package com.dufeng.common.result;

import lombok.Getter;

/**
 * 统一错误码，按业务域划分：1xxx 用户、2xxx 商品、3xxx 订单、4xxx 支付、5xxx 平台。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "success"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无操作权限"),
    NOT_FOUND(404, "资源不存在"),

    // 用户域
    USER_NOT_FOUND(10001, "用户不存在或已被禁用"),
    VERIFY_CODE_ERROR(10002, "验证码错误或已过期"),
    USER_EXISTS(10003, "账号已存在"),
    LOGIN_FAILED(10004, "账号或密码错误"),
    OLD_PASSWORD_ERROR(10005, "原密码错误"),
    SMS_TOO_FREQUENT(10006, "验证码发送过于频繁"),
    TOKEN_INVALID(10007, "登录凭证无效"),
    SMS_DAILY_LIMIT(10008, "今日验证码发送次数已达上限"),
    LOGIN_LOCKED(10009, "登录失败次数过多，请稍后再试"),
    SMS_VERIFY_CODE_REQUIRED(10010, "请填写短信验证码"),

    // 商品域
    GOODS_NOT_FOUND(20001, "商品不存在或已下架"),
    STOCK_NOT_ENOUGH(20002, "库存不足"),
    SKU_NOT_FOUND(20003, "商品规格不存在"),
    CATEGORY_NOT_FOUND(20004, "类目不存在"),
    GOODS_STATUS_ERROR(20005, "商品状态不允许该操作"),

    // 订单域
    ORDER_NOT_FOUND(30001, "订单不存在"),
    ORDER_STATUS_INVALID(30002, "订单状态不允许该操作"),
    ORDER_AMOUNT_ERROR(30003, "订单金额校验失败"),
    CART_EMPTY(30004, "购物车为空"),
    DUPLICATE_REQUEST(30005, "请求正在处理中，请勿重复提交"),

    // 支付域
    PAY_SIGN_ERROR(40001, "支付签名校验失败"),
    PAY_NOT_FOUND(40002, "支付记录不存在"),
    PAY_STATUS_INVALID(40003, "支付状态不允许该操作"),
    PAY_ORDER_MISMATCH(40004, "支付流水与订单不匹配"),
    PAY_AMOUNT_MISMATCH(40005, "回调金额与应付金额不一致"),

    // 平台域
    NO_PERMISSION(50001, "无操作权限"),
    MERCHANT_NOT_FOUND(50002, "商家不存在"),
    MERCHANT_AUDIT_INVALID(50003, "商家审核状态不允许该操作");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
