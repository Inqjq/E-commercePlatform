package com.dufeng.module.address.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = ValidationMessages.RECEIVER_NOT_BLANK)
    private String receiver;

    @NotBlank(message = ValidationMessages.CONTACT_PHONE_NOT_BLANK)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = ValidationMessages.PHONE_PATTERN)
    private String phone;

    @NotBlank(message = ValidationMessages.PROVINCE_NOT_BLANK)
    private String province;

    @NotBlank(message = ValidationMessages.CITY_NOT_BLANK)
    private String city;

    @NotBlank(message = ValidationMessages.DISTRICT_NOT_BLANK)
    private String district;

    @NotBlank(message = ValidationMessages.DETAIL_NOT_BLANK)
    private String detail;

    private Integer isDefault;
}
