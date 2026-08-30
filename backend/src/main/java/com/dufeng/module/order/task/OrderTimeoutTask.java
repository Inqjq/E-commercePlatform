package com.dufeng.module.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dufeng.common.constant.BusinessMessages;
import com.dufeng.module.order.constant.OrderStatus;
import com.dufeng.module.order.entity.Orders;
import com.dufeng.module.order.mapper.OrdersMapper;
import com.dufeng.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 超时未支付订单自动关单任务：释放被预占的库存，防止恶意锁库存。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrdersMapper ordersMapper;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
    public void closeExpiredOrders() {
        List<Orders> expired = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, OrderStatus.WAIT_PAY)
                .lt(Orders::getExpireTime, LocalDateTime.now())
                .last("LIMIT 100"));
        if (expired.isEmpty()) {
            return;
        }
        int closed = 0;
        for (Orders order : expired) {
            // CAS 关单：与用户取消/支付回调并发时只有一方生效，失败方跳过
            if (orderService.transitionToCancelled(order, BusinessMessages.ORDER_CANCEL_TIMEOUT)) {
                closed++;
            }
        }
        log.info("[订单超时] 本轮扫描过期订单 {} 笔，成功关单 {} 笔", expired.size(), closed);
    }
}
