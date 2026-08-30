package com.dufeng.module.admin.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.admin.annotation.AdminAudit;
import com.dufeng.module.admin.dto.AuditRequest;
import com.dufeng.module.admin.dto.MerchantPageQuery;
import com.dufeng.module.admin.service.AdminService;
import com.dufeng.module.merchant.dto.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台商家审核接口（需 ADMIN 角色）。
 */
@Tag(name = "平台-商家")
@RestController
@RequestMapping("/api/admin/merchant")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminService adminService;

    @Operation(summary = "商家列表")
    @GetMapping
    public Result<PageResult<MerchantVO>> list(MerchantPageQuery query) {
        return Result.success(adminService.pageMerchants(query));
    }

    @Operation(summary = "商家入驻审核")
    @AdminAudit(module = "商家管理", action = "商家入驻审核")
    @PostMapping("/{id}/audit")
    public Result<MerchantVO> audit(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return Result.success(adminService.auditMerchant(id, request.getApprove(), request.getReason()));
    }
}
