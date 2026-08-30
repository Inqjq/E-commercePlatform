package com.dufeng.module.user.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = ValidationMessages.OLD_PASSWORD_NOT_BLANK)
    private String oldPassword;

    @NotBlank(message = ValidationMessages.NEW_PASSWORD_NOT_BLANK)
    @Size(min = 6, max = 32, message = ValidationMessages.NEW_PASSWORD_SIZE)
    private String newPassword;
}
