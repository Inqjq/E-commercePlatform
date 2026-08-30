package com.dufeng.common.constant;

/**
 * 业务提示文案统一管理。
 */
public final class BusinessMessages {

    public static final String PHONE_ALREADY_REGISTERED = "手机号已被注册";
    public static final String ADDRESS_NOT_FOUND = "地址不存在";
    public static final String CART_ITEM_NOT_FOUND = "购物车条目不存在";
    public static final String ORDER_CANCEL_BY_USER = "用户取消";
    public static final String ORDER_CANCEL_TIMEOUT = "超时未支付，系统自动取消";
    public static final String ORDER_ITEMS_EMPTY = "下单商品不能为空";
    public static final String GOODS_STOCK_NOT_ENOUGH = "商品[%s]库存不足";
    public static final String MERCHANT_ALREADY_APPROVED = "商家已审核通过，无需重复提交";
    public static final String SHOP_NOT_OPEN = "店铺尚未开通";
    public static final String REVIEW_ONLY_COMPLETED = "订单完成后才能评价";
    public static final String REVIEW_ALREADY_SUBMITTED = "该商品已评价";

    private BusinessMessages() {
    }
}
