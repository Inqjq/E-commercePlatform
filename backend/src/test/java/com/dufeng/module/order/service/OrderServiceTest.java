package com.dufeng.module.order.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dufeng.config.OrderProperties;
import com.dufeng.module.address.service.AddressService;
import com.dufeng.module.cart.service.CartService;
import com.dufeng.module.goods.mapper.SkuMapper;
import com.dufeng.module.goods.service.GoodsService;
import com.dufeng.module.merchant.mapper.ShopMapper;
import com.dufeng.module.order.entity.OrderItem;
import com.dufeng.module.order.entity.Orders;
import com.dufeng.module.order.mapper.OrderItemMapper;
import com.dufeng.module.order.mapper.OrdersMapper;
import com.dufeng.module.order.service.impl.OrderServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 订单状态 CAS 流转单测：并发冲突时不回补库存，胜出方负责回补。
 */
class OrderServiceTest {

    private OrdersMapper ordersMapper;
    private OrderItemMapper orderItemMapper;
    private SkuMapper skuMapper;
    private OrderService orderService;

    @BeforeAll
    static void initEntityMetadata() {
        // 纯单测环境下手工初始化 MyBatis-Plus 实体元数据，LambdaWrapper 依赖
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Orders.class);
        TableInfoHelper.initTableInfo(assistant, OrderItem.class);
    }

    @BeforeEach
    void setUp() {
        ordersMapper = mock(OrdersMapper.class);
        orderItemMapper = mock(OrderItemMapper.class);
        skuMapper = mock(SkuMapper.class);
        orderService = new OrderServiceImpl(ordersMapper, orderItemMapper, skuMapper,
                mock(GoodsService.class), mock(AddressService.class), mock(CartService.class),
                mock(ShopMapper.class), mock(StringRedisTemplate.class), new OrderProperties());
    }

    private static Orders order(Long id, String orderNo) {
        Orders order = new Orders();
        order.setId(id);
        order.setOrderNo(orderNo);
        return order;
    }

    @Test
    @DisplayName("CAS 取消成功后按订单项回补库存")
    void cancelSuccessRestoresStock() {
        when(ordersMapper.update(any(), any())).thenReturn(1);
        OrderItem first = new OrderItem();
        first.setSkuId(11L);
        first.setQuantity(2);
        OrderItem second = new OrderItem();
        second.setSkuId(22L);
        second.setQuantity(3);
        when(orderItemMapper.selectList(any())).thenReturn(List.of(first, second));

        boolean result = orderService.transitionToCancelled(order(1L, "ORDER1"), "测试取消");

        assertTrue(result);
        verify(skuMapper).restoreStock(11L, 2);
        verify(skuMapper).restoreStock(22L, 3);
    }

    @Test
    @DisplayName("CAS 取消失败（并发冲突）时不回补库存")
    void cancelConflictSkipsStockRestore() {
        when(ordersMapper.update(any(), any())).thenReturn(0);

        boolean result = orderService.transitionToCancelled(order(1L, "ORDER1"), "测试取消");

        assertFalse(result);
        verifyNoInteractions(orderItemMapper);
        verify(skuMapper, never()).restoreStock(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("tryMarkPaid 仅在 CAS 命中时返回 true")
    void tryMarkPaidReflectsCasResult() {
        when(ordersMapper.update(any(), any())).thenReturn(1).thenReturn(0);

        assertTrue(orderService.tryMarkPaid(1L));
        assertFalse(orderService.tryMarkPaid(1L));
    }
}
