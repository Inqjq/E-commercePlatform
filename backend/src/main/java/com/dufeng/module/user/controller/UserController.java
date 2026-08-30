package com.dufeng.module.user.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.user.dto.ChangePasswordRequest;
import com.dufeng.module.user.dto.UpdateProfileRequest;
import com.dufeng.module.user.dto.UserVO;
import com.dufeng.module.user.service.UserService;
import com.dufeng.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台用户个人中心接口。
 */
@Tag(name = "前台-用户中心")
@RestController
@RequestMapping("/api/portal/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.getCurrent(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.success(userService.updateProfile(SecurityUtils.currentUserId(), request));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(SecurityUtils.currentUserId(), request);
        return Result.success();
    }
}
