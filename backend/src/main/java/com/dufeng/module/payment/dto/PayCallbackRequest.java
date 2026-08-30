package com.dufeng.module.payment.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayCallbackRequest {

    @NotBlank(message = ValidationMessages.ORDER_NO_NOT_BLANK)
    private String orderNo;

    @NotBlank(message = ValidationMessages.PAY_NO_NOT_BLANK)
    private String payNo;

    @NotBlank(message = ValidationMessages.PAY_CHANNEL_NOT_BLANK)
    private String channel;

    @NotNull(message = ValidationMessages.PAY_AMOUNT_NOT_NULL)
    private BigDecimal amount;

    /** 第三方交易号。 */
    private String tradeNo;

    /** 回调签名。 */
    private String sign;

    /** 1 成功，2 失败。 */
    private Integer status;
}
