package com.dufeng.module.cart.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.cart.dto.CartAddRequest;
import com.dufeng.module.cart.dto.CartCheckRequest;
import com.dufeng.module.cart.dto.CartQuantityRequest;
import com.dufeng.module.cart.dto.CartShopVO;
import com.dufeng.module.cart.service.CartService;
import com.dufeng.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台购物车接口。
 */
@Tag(name = "前台-购物车")
@RestController
@RequestMapping("/api/portal/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "购物车列表(按店铺分组)")
    @GetMapping
    public Result<List<CartShopVO>> list() {
        return Result.success(cartService.list(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartAddRequest request) {
        cartService.add(SecurityUtils.currentUserId(), request.getSkuId(), request.getQuantity());
        return Result.success();
    }

    @Operation(summary = "修改数量")
    @PutMapping("/{id}/quantity")
    public Result<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody CartQuantityRequest request) {
        cartService.updateQuantity(SecurityUtils.currentUserId(), id, request.getQuantity());
        return Result.success();
    }

    @Operation(summary = "勾选/取消勾选")
    @PutMapping("/{id}/checked")
    public Result<Void> setChecked(@PathVariable Long id, @Valid @RequestBody CartCheckRequest request) {
        cartService.setChecked(SecurityUtils.currentUserId(), id, request.getChecked());
        return Result.success();
    }

    @Operation(summary = "删除购物车条目")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(SecurityUtils.currentUserId(), id);
        return Result.success();
    }
}
