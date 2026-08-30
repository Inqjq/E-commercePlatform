package com.dufeng.module.user.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SmsCodeRequest {

    @NotBlank(message = ValidationMessages.PHONE_NOT_BLANK)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = ValidationMessages.PHONE_PATTERN)
    private String phone;
}
