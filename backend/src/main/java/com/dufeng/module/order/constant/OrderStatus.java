package com.dufeng.module.order.constant;

/**
 * 订单状态枚举。
 */
public final class OrderStatus {

    public static final int WAIT_PAY = 0;
    public static final int WAIT_SHIP = 1;
    public static final int WAIT_RECEIVE = 2;
    public static final int COMPLETED = 3;
    public static final int CANCELLED = 4;
    public static final int AFTER_SALE = 5;

    private OrderStatus() {
    }

    public static String text(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case WAIT_PAY -> "待付款";
            case WAIT_SHIP -> "待发货";
            case WAIT_RECEIVE -> "待收货";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case AFTER_SALE -> "售后中";
            default -> "未知";
        };
    }
}
