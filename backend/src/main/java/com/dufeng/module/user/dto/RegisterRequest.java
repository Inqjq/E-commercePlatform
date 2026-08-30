package com.dufeng.module.user.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = ValidationMessages.USERNAME_NOT_BLANK)
    @Size(min = 4, max = 32, message = ValidationMessages.USERNAME_SIZE)
    private String username;

    @NotBlank(message = ValidationMessages.PASSWORD_NOT_BLANK)
    @Size(min = 6, max = 32, message = ValidationMessages.PASSWORD_SIZE)
    private String password;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = ValidationMessages.PHONE_PATTERN)
    private String phone;

    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = ValidationMessages.EMAIL_PATTERN)
    private String email;

    private String nickname;

    private String verifyCode;
}
