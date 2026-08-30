package com.dufeng.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order_item")
public class OrderItem extends BaseEntity {

    private Long orderId;

    private String orderNo;

    private Long shopId;

    private Long goodsId;

    private Long skuId;

    private String goodsTitle;

    private String specText;

    private String image;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;

    /** 0 未评价，1 已评价。 */
    private Integer reviewed;
}
