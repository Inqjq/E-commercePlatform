package com.dufeng.module.order.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderShipRequest {

    @NotBlank(message = ValidationMessages.LOGISTICS_COMPANY_NOT_BLANK)
    private String logisticsCompany;

    @NotBlank(message = ValidationMessages.LOGISTICS_NO_NOT_BLANK)
    private String logisticsNo;
}
