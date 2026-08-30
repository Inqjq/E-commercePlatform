package com.dufeng.module.user.controller;

import com.dufeng.common.result.Result;
import com.dufeng.common.constant.SecurityConstants;
import com.dufeng.module.user.dto.LoginRequest;
import com.dufeng.module.user.dto.LoginResponse;
import com.dufeng.module.user.dto.RegisterRequest;
import com.dufeng.module.user.dto.ResetPasswordRequest;
import com.dufeng.module.user.dto.SmsCodeRequest;
import com.dufeng.module.user.service.SmsCodeService;
import com.dufeng.module.user.service.UserService;
import com.dufeng.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台认证接口。
 */
@Tag(name = "前台-认证")
@RestController
@RequestMapping("/api/portal/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SmsCodeService smsCodeService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getAccount(), request.getPassword()));
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        smsCodeService.sendCode(request.getPhone());
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = SecurityConstants.AUTH_HEADER, required = false) String authorization) {
        Long userId = SecurityUtils.currentUserId();
        String token = authorization == null ? null
                : authorization.startsWith(SecurityConstants.BEARER_PREFIX)
                ? authorization.substring(SecurityConstants.BEARER_PREFIX.length()) : authorization;
        userService.logout(token, userId);
        return Result.success();
    }
}
