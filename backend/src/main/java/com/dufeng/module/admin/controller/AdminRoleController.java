package com.dufeng.module.admin.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.admin.annotation.AdminAudit;
import com.dufeng.module.admin.dto.RoleRequest;
import com.dufeng.module.admin.entity.Role;
import com.dufeng.module.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台角色权限接口（需 ADMIN 角色）。
 */
@Tag(name = "平台-角色权限")
@RestController
@RequestMapping("/api/admin/role")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminService adminService;

    @Operation(summary = "角色列表")
    @GetMapping
    public Result<List<Role>> list() {
        return Result.success(adminService.listRoles());
    }

    @Operation(summary = "新增角色")
    @AdminAudit(module = "权限管理", action = "新增角色")
    @PostMapping
    public Result<Role> create(@Valid @RequestBody RoleRequest request) {
        return Result.success(adminService.createRole(request));
    }
}
