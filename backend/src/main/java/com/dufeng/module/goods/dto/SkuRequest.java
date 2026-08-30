package com.dufeng.module.goods.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuRequest {

    private Long id;

    @NotNull(message = ValidationMessages.SKU_PRICE_NOT_NULL)
    private BigDecimal price;

    @NotNull(message = ValidationMessages.SKU_STOCK_NOT_NULL)
    @Min(value = 0, message = ValidationMessages.STOCK_NON_NEGATIVE)
    private Integer stock;

    private String skuCode;
    private String specJson;
    private String specText;
    private String image;
    private BigDecimal weight;
}
