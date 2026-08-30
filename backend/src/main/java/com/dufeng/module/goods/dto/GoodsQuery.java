package com.dufeng.module.goods.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsQuery {

    private long current = 1;
    private long size = 10;
    private Long categoryId;
    private Long brandId;
    private Long shopId;
    private String keyword;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    /** sort: default | sales | price_asc | price_desc。 */
    private String sort;
}
