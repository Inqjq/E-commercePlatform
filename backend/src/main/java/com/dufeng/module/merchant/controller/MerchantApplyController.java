package com.dufeng.module.merchant.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.merchant.dto.MerchantApplyRequest;
import com.dufeng.module.merchant.dto.MerchantVO;
import com.dufeng.module.merchant.service.MerchantService;
import com.dufeng.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台商家入驻申请接口。
 */
@Tag(name = "前台-商家入驻")
@RestController
@RequestMapping("/api/portal/merchant")
@RequiredArgsConstructor
public class MerchantApplyController {

    private final MerchantService merchantService;

    @Operation(summary = "提交入驻申请")
    @PostMapping("/apply")
    public Result<MerchantVO> apply(@Valid @RequestBody MerchantApplyRequest request) {
        return Result.success(merchantService.apply(SecurityUtils.currentUserId(), request));
    }

    @Operation(summary = "查询入驻状态")
    @GetMapping("/status")
    public Result<MerchantVO> status() {
        return Result.success(merchantService.getMyMerchant(SecurityUtils.currentUserId()));
    }
}
