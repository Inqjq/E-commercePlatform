package com.dufeng.module.admin.dto;

import lombok.Data;

@Data
public class MerchantPageQuery {

    private long current = 1;
    private long size = 10;
    private Integer auditStatus;
    private String keyword;
}
