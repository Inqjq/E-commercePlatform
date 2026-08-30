package com.dufeng.module.merchant.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MerchantApplyRequest {

    @NotBlank(message = ValidationMessages.MERCHANT_NAME_NOT_BLANK)
    private String name;

    @NotBlank(message = ValidationMessages.LICENSE_NO_NOT_BLANK)
    private String licenseNo;

    @NotBlank(message = ValidationMessages.LEGAL_PERSON_NOT_BLANK)
    private String legalPerson;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = ValidationMessages.MERCHANT_PHONE_PATTERN)
    private String contactPhone;

    private String categoryIds;
}
