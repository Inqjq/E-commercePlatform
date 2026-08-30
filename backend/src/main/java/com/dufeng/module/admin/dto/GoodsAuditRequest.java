package com.dufeng.module.admin.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoodsAuditRequest {

    @NotNull(message = ValidationMessages.AUDIT_RESULT_NOT_NULL)
    private Boolean approve;

    private String reason;
}
