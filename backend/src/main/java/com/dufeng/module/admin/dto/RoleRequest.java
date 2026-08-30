package com.dufeng.module.admin.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RoleRequest {

    @NotBlank(message = ValidationMessages.ROLE_NAME_NOT_BLANK)
    private String name;

    @NotBlank(message = ValidationMessages.ROLE_CODE_NOT_BLANK)
    private String code;

    private String description;
    private List<Long> permissionIds;
}
