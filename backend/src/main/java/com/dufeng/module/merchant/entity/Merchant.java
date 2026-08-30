package com.dufeng.module.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant")
public class Merchant extends BaseEntity {

    private Long accountId;

    private String name;

    private String licenseNo;

    private String legalPerson;

    private String contactPhone;

    private String categoryIds;

    /** 0 待审核，1 通过，2 驳回，3 冻结。 */
    private Integer auditStatus;

    private String auditReason;

    private Integer status;
}
