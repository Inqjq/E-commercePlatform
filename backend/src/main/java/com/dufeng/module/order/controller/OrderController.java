package com.dufeng.module.order.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.order.dto.OrderCreateRequest;
import com.dufeng.module.order.dto.OrderCreateResponse;
import com.dufeng.module.order.dto.OrderQuery;
import com.dufeng.module.order.dto.OrderVO;
import com.dufeng.module.order.service.OrderService;
import com.dufeng.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台订单接口。
 */
@Tag(name = "前台-订单")
@RestController
@RequestMapping("/api/portal/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单(支持拆单)")
    @PostMapping
    public Result<List<OrderCreateResponse>> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderService.createOrders(SecurityUtils.currentUserId(), request));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(OrderQuery query) {
        return Result.success(orderService.pageQuery(SecurityUtils.currentUserId(), query));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.success(orderService.detail(SecurityUtils.currentUserId(), orderNo));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancel(SecurityUtils.currentUserId(), orderNo);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{orderNo}/confirm")
    public Result<Void> confirm(@PathVariable String orderNo) {
        orderService.confirmReceipt(SecurityUtils.currentUserId(), orderNo);
        return Result.success();
    }
}
