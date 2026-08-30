package com.dufeng.module.merchant.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.merchant.dto.ShopRequest;
import com.dufeng.module.merchant.dto.ShopVO;
import com.dufeng.module.merchant.service.MerchantService;
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
 * 商家店铺管理接口（需 MERCHANT 角色）。
 */
@Tag(name = "商家-店铺")
@RestController
@RequestMapping("/api/merchant/shop")
@RequiredArgsConstructor
public class MerchantShopController {

    private final MerchantService merchantService;

    @Operation(summary = "获取我的店铺")
    @GetMapping
    public Result<ShopVO> get() {
        return Result.success(merchantService.getShopByAccountId(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "更新店铺信息")
    @PutMapping
    public Result<ShopVO> update(@Valid @RequestBody ShopRequest request) {
        return Result.success(merchantService.updateShop(SecurityUtils.currentUserId(), request));
    }
}
