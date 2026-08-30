package com.dufeng.module.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 审计日志分页查询。
 */
@Data
public class AuditLogQuery {

    @Min(1)
    private long current = 1;

    @Min(1)
    @Max(200)
    private long size = 20;

    private String module;

    private String operator;

    private String keyword;
}
