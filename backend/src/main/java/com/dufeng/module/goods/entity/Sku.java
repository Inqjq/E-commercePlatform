package com.dufeng.module.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sku")
public class Sku extends BaseEntity {

    private Long goodsId;

    private String skuCode;

    private String specJson;

    private String specText;

    private String image;

    private BigDecimal price;

    private Integer stock;

    private BigDecimal weight;

    /** 0 下架，1 上架。 */
    private Integer status;
}
