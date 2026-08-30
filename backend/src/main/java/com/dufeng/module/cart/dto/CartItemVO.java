package com.dufeng.module.cart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {

    private Long id;
    private Long skuId;
    private Long goodsId;
    private Long shopId;
    private Integer quantity;
    private Integer checked;
    private String goodsTitle;
    private String goodsImage;
    private String specText;
    private BigDecimal price;
    private Integer stock;
    /** 失效标记：true 表示商品已下架或库存不足。 */
    private Boolean invalid;
}
