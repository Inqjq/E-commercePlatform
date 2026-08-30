package com.dufeng.module.cart.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartQuantityRequest {

    @NotNull(message = ValidationMessages.QUANTITY_NOT_NULL)
    @Min(value = 1, message = ValidationMessages.QUANTITY_MIN)
    private Integer quantity;
}
