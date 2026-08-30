package com.dufeng.module.merchant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantVO {

    private Long id;
    private Long accountId;
    private String name;
    private String licenseNo;
    private String legalPerson;
    private String contactPhone;
    private String categoryIds;
    private Integer auditStatus;
    private String auditReason;
    private Integer status;
    private LocalDateTime createTime;
}
