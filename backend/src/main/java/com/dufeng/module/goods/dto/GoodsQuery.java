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
    /** 商品状态：0 草稿，1 待审核，2 已上架，3 已下架，4 审核驳回。 */
    private Integer status;
    /** 审核状态：0 未提交，1 待审核，2 通过，3 驳回。 */
    private Integer auditStatus;
}
