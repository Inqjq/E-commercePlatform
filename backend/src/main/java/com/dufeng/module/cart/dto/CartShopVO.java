package com.dufeng.module.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CartShopVO {

    private Long shopId;
    private String shopName;
    private List<CartItemVO> items = new ArrayList<>();
    private BigDecimal checkedAmount;
}
