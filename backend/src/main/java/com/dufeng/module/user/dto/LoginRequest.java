package com.dufeng.module.user.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = ValidationMessages.ACCOUNT_NOT_BLANK)
    private String account;

    @NotBlank(message = ValidationMessages.PASSWORD_NOT_BLANK)
    private String password;
}
