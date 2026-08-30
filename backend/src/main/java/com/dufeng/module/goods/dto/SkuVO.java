package com.dufeng.module.goods.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuVO {

    private Long id;
    private Long goodsId;
    private String skuCode;
    private String specJson;
    private String specText;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private BigDecimal weight;
    private Integer status;
}
