package com.dufeng.module.admin.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.admin.annotation.AdminAudit;
import com.dufeng.module.admin.dto.GoodsAuditRequest;
import com.dufeng.module.admin.service.AdminService;
import com.dufeng.module.goods.dto.GoodsQuery;
import com.dufeng.module.goods.dto.GoodsVO;
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
 * 平台商品审核接口（需 ADMIN 角色）。
 */
@Tag(name = "平台-商品")
@RestController
@RequestMapping("/api/admin/goods")
@RequiredArgsConstructor
public class AdminGoodsController {

    private final AdminService adminService;

    @Operation(summary = "商品列表(含未审核)")
    @GetMapping
    public Result<PageResult<GoodsVO>> list(GoodsQuery query) {
        return Result.success(adminService.pageGoods(query));
    }

    @Operation(summary = "商品上架审核")
    @AdminAudit(module = "商品管理", action = "商品上架审核")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody GoodsAuditRequest request) {
        adminService.auditGoods(id, request);
        return Result.success();
    }

    @Operation(summary = "强制下架")
    @AdminAudit(module = "商品管理", action = "商品强制下架")
    @PostMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        adminService.forceOffline(id);
        return Result.success();
    }
}
