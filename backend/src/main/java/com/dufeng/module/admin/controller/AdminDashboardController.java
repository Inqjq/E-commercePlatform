package com.dufeng.module.admin.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.admin.dto.DashboardVO;
import com.dufeng.module.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台数据看板接口（需 ADMIN 角色）。
 */
@Tag(name = "平台-数据看板")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminService adminService;

    @Operation(summary = "看板概览")
    @GetMapping("/overview")
    public Result<DashboardVO> overview() {
        return Result.success(adminService.dashboard());
    }
}
