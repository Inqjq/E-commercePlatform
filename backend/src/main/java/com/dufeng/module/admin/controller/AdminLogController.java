package com.dufeng.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.admin.dto.AuditLogQuery;
import com.dufeng.module.admin.entity.AuditLog;
import com.dufeng.module.admin.mapper.AuditLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台审计日志接口（需 ADMIN 角色）。
 */
@Tag(name = "平台-审计日志")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final AuditLogMapper auditLogMapper;

    @Operation(summary = "审计日志列表")
    @GetMapping
    public Result<PageResult<AuditLog>> list(AuditLogQuery query) {
        Page<AuditLog> page = auditLogMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()),
                new LambdaQueryWrapper<AuditLog>()
                        .eq(StringUtils.hasText(query.getModule()), AuditLog::getModule, query.getModule())
                        .like(StringUtils.hasText(query.getOperator()), AuditLog::getOperatorName, query.getOperator())
                        .orderByDesc(AuditLog::getCreateTime)
                        .orderByDesc(AuditLog::getId));
        return Result.success(PageResult.of(page, item -> item));
    }
}
