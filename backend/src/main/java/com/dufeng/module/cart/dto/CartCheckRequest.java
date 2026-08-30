package com.dufeng.module.cart.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartCheckRequest {

    @NotNull(message = ValidationMessages.CHECKED_NOT_NULL)
    private Boolean checked;
}
