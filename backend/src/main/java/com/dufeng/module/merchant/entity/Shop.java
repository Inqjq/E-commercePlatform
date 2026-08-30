package com.dufeng.module.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_shop")
public class Shop extends BaseEntity {

    private Long merchantId;

    private String name;

    private String logo;

    private String intro;

    private String servicePhone;

    /** 0 关闭，1 营业。 */
    private Integer status;
}
