package com.dufeng.module.order.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.merchant.service.MerchantService;
import com.dufeng.module.order.dto.OrderQuery;
import com.dufeng.module.order.dto.OrderShipRequest;
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

/**
 * 商家订单管理接口（需 MERCHANT 角色）。
 */
@Tag(name = "商家-订单")
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final OrderService orderService;
    private final MerchantService merchantService;

    private Long currentShopId() {
        return merchantService.getShopIdByAccountId(SecurityUtils.currentUserId());
    }

    @Operation(summary = "店铺订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(OrderQuery query) {
        return Result.success(orderService.merchantPage(currentShopId(), query));
    }

    @Operation(summary = "发货")
    @PostMapping("/{orderNo}/ship")
    public Result<Void> ship(@PathVariable String orderNo, @Valid @RequestBody OrderShipRequest request) {
        orderService.ship(currentShopId(), orderNo, request.getLogisticsCompany(), request.getLogisticsNo());
        return Result.success();
    }
}
