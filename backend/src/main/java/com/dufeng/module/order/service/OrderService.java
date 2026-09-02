package com.dufeng.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dufeng.common.result.PageResult;
import com.dufeng.module.order.dto.OrderCreateRequest;
import com.dufeng.module.order.dto.OrderCreateResponse;
import com.dufeng.module.order.dto.OrderQuery;
import com.dufeng.module.order.dto.OrderVO;
import com.dufeng.module.order.entity.Orders;
import java.util.List;

public interface OrderService {

    List<OrderCreateResponse> createOrders(Long userId, OrderCreateRequest request);

    PageResult<OrderVO> pageQuery(Long userId, OrderQuery query);

    OrderVO detail(Long userId, String orderNo);

    PageResult<OrderVO> merchantPage(Long shopId, OrderQuery query);

    void ship(Long shopId, String orderNo, String logisticsCompany, String logisticsNo);

    void cancel(Long userId, String orderNo);

    void confirmReceipt(Long userId, String orderNo);

    boolean transitionToCancelled(Orders order, String reason);

    boolean tryMarkPaid(Long orderId);

    Orders getByNo(String orderNo);

    Orders getById(Long id);

}
