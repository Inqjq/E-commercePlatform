package com.dufeng.module.order.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStatusTest {

    @Test
    void shouldMapStatusToChineseText() {
        assertEquals("待付款", OrderStatus.text(OrderStatus.WAIT_PAY));
        assertEquals("待发货", OrderStatus.text(OrderStatus.WAIT_SHIP));
        assertEquals("待收货", OrderStatus.text(OrderStatus.WAIT_RECEIVE));
        assertEquals("已完成", OrderStatus.text(OrderStatus.COMPLETED));
        assertEquals("已取消", OrderStatus.text(OrderStatus.CANCELLED));
    }
}
