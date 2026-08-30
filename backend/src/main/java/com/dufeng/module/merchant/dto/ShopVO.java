package com.dufeng.module.merchant.dto;

import lombok.Data;

@Data
public class ShopVO {

    private Long id;
    private Long merchantId;
    private String name;
    private String logo;
    private String intro;
    private String servicePhone;
    private Integer status;
}
