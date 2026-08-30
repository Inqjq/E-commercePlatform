package com.dufeng.module.merchant.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopRequest {

    @NotBlank(message = ValidationMessages.SHOP_NAME_NOT_BLANK)
    private String name;

    private String logo;
    private String intro;
    private String servicePhone;
}
