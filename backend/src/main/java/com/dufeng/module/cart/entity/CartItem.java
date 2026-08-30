package com.dufeng.module.cart.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cart_item")
public class CartItem extends BaseEntity {

    private Long userId;

    private Long skuId;

    private Long goodsId;

    private Long shopId;

    private Integer quantity;

    /** 0 未选中，1 选中。 */
    private Integer checked;
}
