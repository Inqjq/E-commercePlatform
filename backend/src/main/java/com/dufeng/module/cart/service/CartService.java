package com.dufeng.module.cart.service;

import com.dufeng.module.cart.dto.CartShopVO;
import java.util.List;

public interface CartService {

    void add(Long userId, Long skuId, Integer quantity);

    List<CartShopVO> list(Long userId);

    void updateQuantity(Long userId, Long id, Integer quantity);

    void setChecked(Long userId, Long id, boolean checked);

    void remove(Long userId, Long id);

    void clearBySkuIds(Long userId, List<Long> skuIds);

}
