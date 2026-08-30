package com.dufeng.module.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long id;
    private Long goodsId;
    private Long skuId;
    private String goodsTitle;
    private String specText;
    private String image;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer reviewed;
}
