package com.dufeng.module.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dufeng.module.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
