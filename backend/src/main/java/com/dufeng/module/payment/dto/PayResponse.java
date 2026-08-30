package com.dufeng.module.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayResponse {

    private String orderNo;
    private String payNo;
    private BigDecimal amount;
    private String channel;
    /** 第三方拉起支付所需的预支付参数（模拟）。 */
    private String prepayParams;
    /** 支付宝收银台跳转地址（启用支付宝时返回，前端直接跳转）。 */
    private String payUrl;
}
