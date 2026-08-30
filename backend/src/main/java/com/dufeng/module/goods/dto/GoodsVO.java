package com.dufeng.module.goods.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsVO {

    private Long id;
    private Long shopId;
    private String shopName;
    private Long categoryId;
    private Long brandId;
    private String title;
    private String subtitle;
    private String mainImage;
    private String images;
    private String detail;
    private BigDecimal price;
    private Integer status;
    private Integer auditStatus;
    private Integer sales;
    private LocalDateTime createTime;
}
