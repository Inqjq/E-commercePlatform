package com.dufeng.module.payment.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayRequest {

    @NotBlank(message = ValidationMessages.PAY_CHANNEL_NOT_BLANK)
    private String channel;
}
