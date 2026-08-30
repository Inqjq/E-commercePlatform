package com.dufeng.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_orders")
public class Orders extends BaseEntity {

    private String orderNo;

    private Long userId;

    private Long shopId;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal freightAmount;

    private BigDecimal payAmount;

    /** 0 待付款，1 待发货，2 待收货，3 已完成，4 已取消，5 售后中。 */
    private Integer status;

    private String receiver;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    private String cancelReason;

    private LocalDateTime expireTime;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime finishTime;

    private String logisticsCompany;

    private String logisticsNo;

    @Version
    private Integer version;
}
