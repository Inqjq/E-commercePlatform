package com.dufeng.module.user.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 64, message = ValidationMessages.NICKNAME_SIZE)
    private String nickname;

    private String avatar;

    private Integer gender;

    @Size(max = 200, message = ValidationMessages.INTRO_SIZE)
    private String intro;
}
