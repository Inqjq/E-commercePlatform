package com.dufeng.module.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_goods")
public class Goods extends BaseEntity {

    private Long shopId;

    private Long categoryId;

    private Long brandId;

    private String title;

    private String subtitle;

    private String mainImage;

    private String images;

    private String detail;

    private BigDecimal price;

    /** 0 草稿，1 待审核，2 已上架，3 已下架，4 审核驳回。 */
    private Integer status;

    /** 0 未提交，1 待审核，2 通过，3 驳回。 */
    private Integer auditStatus;

    private Integer sales;
}
