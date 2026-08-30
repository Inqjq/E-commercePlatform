package com.dufeng.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment")
public class Payment extends BaseEntity {

    private Long orderId;

    private String orderNo;

    private String payNo;

    private Long userId;

    /** alipay / wechat / balance。 */
    private String channel;

    private BigDecimal amount;

    /** 0 待支付，1 成功，2 失败，3 已退款。 */
    private Integer status;

    private LocalDateTime callbackTime;
}
