package com.dufeng.module.admin.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.admin.annotation.AdminAudit;
import com.dufeng.module.admin.dto.UserPageQuery;
import com.dufeng.module.admin.service.AdminService;
import com.dufeng.module.user.dto.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台用户管理接口（需 ADMIN 角色）。
 */
@Tag(name = "平台-用户")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @Operation(summary = "用户列表")
    @GetMapping
    public Result<PageResult<UserVO>> list(UserPageQuery query) {
        return Result.success(adminService.pageUsers(query));
    }

    @Operation(summary = "启用/禁用用户")
    @AdminAudit(module = "用户管理", action = "变更用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return Result.success();
    }
}
