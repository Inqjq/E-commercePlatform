package com.dufeng.module.payment.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.payment.dto.PayCallbackRequest;
import com.dufeng.module.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 第三方支付异步回调（公开接口，依赖签名验真）。
 */
@Tag(name = "支付-回调")
@RestController
@RequestMapping("/api/portal/pay/callback")
@RequiredArgsConstructor
public class PayCallbackController {

    private final PaymentService paymentService;

    @Operation(summary = "模拟渠道支付回调")
    @PostMapping
    public Result<Void> callback(@Valid @RequestBody PayCallbackRequest request) {
        paymentService.handleCallback(request);
        return Result.success();
    }

    /**
     * 支付宝异步通知：表单编码参数，响应必须是 "success"/"failure" 纯文本。
     */
    @Operation(summary = "支付宝异步通知")
    @PostMapping(value = "/alipay/notify",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String alipayNotify(@RequestParam Map<String, String> params) {
        return paymentService.handleAlipayNotify(params);
    }
}
