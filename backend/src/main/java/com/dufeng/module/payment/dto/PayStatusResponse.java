package com.dufeng.module.payment.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 支付状态查询结果（供收银台轮询）。
 */
@Data
@Builder
public class PayStatusResponse {

    private String orderNo;

    /** 是否已支付（订单已推进到待发货及之后）。 */
    private boolean paid;
}
