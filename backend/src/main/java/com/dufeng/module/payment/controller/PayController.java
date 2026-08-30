package com.dufeng.module.payment.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.payment.dto.PayRequest;
import com.dufeng.module.payment.dto.PayResponse;
import com.dufeng.module.payment.dto.PayStatusResponse;
import com.dufeng.module.payment.service.PaymentService;
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
 * 前台发起支付接口。
 */
@Tag(name = "前台-支付")
@RestController
@RequestMapping("/api/portal/pay")
@RequiredArgsConstructor
public class PayController {

    private final PaymentService paymentService;

    @Operation(summary = "发起支付")
    @PostMapping("/{orderNo}")
    public Result<PayResponse> pay(@PathVariable String orderNo, @Valid @RequestBody PayRequest request) {
        return Result.success(paymentService.createPay(SecurityUtils.currentUserId(), orderNo, request.getChannel()));
    }

    @Operation(summary = "查询支付状态（收银台轮询，含支付宝主动查单）")
    @GetMapping("/{orderNo}/status")
    public Result<PayStatusResponse> status(@PathVariable String orderNo) {
        return Result.success(paymentService.syncPayStatus(SecurityUtils.currentUserId(), orderNo));
    }
}
