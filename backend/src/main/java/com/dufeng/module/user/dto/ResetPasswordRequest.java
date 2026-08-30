package com.dufeng.module.user.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = ValidationMessages.PHONE_NOT_BLANK)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = ValidationMessages.PHONE_PATTERN)
    private String phone;

    @NotBlank(message = ValidationMessages.VERIFY_CODE_NOT_BLANK)
    private String verifyCode;

    @NotBlank(message = ValidationMessages.NEW_PASSWORD_NOT_BLANK)
    @Size(min = 6, max = 32, message = ValidationMessages.PASSWORD_SIZE)
    private String newPassword;
}
