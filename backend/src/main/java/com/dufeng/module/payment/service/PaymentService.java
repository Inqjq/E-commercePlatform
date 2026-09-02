package com.dufeng.module.payment.service;

import com.dufeng.module.payment.dto.PayCallbackRequest;
import com.dufeng.module.payment.dto.PayResponse;
import com.dufeng.module.payment.dto.PayStatusResponse;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {

    PayResponse createPay(Long userId, String orderNo, String channel);

    void handleCallback(PayCallbackRequest request);

    PayStatusResponse syncPayStatus(Long userId, String orderNo);

    String handleAlipayNotify(Map<String, String> params);

    boolean markGatewayPaid(String payNo, BigDecimal paidAmount);

}
