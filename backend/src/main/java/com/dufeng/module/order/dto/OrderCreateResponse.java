package com.dufeng.module.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderCreateResponse {

    private Long id;
    private String orderNo;
    private BigDecimal payAmount;
}
