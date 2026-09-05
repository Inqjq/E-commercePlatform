package com.dufeng.module.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dufeng.module.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /** 某商品此前被逻辑删除（deleted=1），仍占用 (user_id,sku_id) 唯一键；重新加购时复活该行。 */
    @Update("UPDATE t_cart_item SET deleted=0, quantity=#{quantity}, checked=1, update_time=NOW() " +
            "WHERE user_id=#{userId} AND sku_id=#{skuId} AND deleted=1")
    int restoreSoftDeleted(@Param("userId") Long userId, @Param("skuId") Long skuId,
                           @Param("quantity") Integer quantity);

    /** 并发/极端情况下新增撞唯一键时，改为给现有有效行累加数量。 */
    @Update("UPDATE t_cart_item SET quantity=quantity+#{quantity}, checked=1, update_time=NOW() " +
            "WHERE user_id=#{userId} AND sku_id=#{skuId} AND deleted=0")
    int increaseQuantity(@Param("userId") Long userId, @Param("skuId") Long skuId,
                         @Param("quantity") Integer quantity);
}
