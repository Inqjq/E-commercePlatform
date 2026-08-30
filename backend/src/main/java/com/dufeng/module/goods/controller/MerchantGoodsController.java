package com.dufeng.module.goods.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.goods.dto.GoodsDetailVO;
import com.dufeng.module.goods.dto.GoodsQuery;
import com.dufeng.module.goods.dto.GoodsRequest;
import com.dufeng.module.goods.dto.GoodsVO;
import com.dufeng.module.goods.service.GoodsService;
import com.dufeng.module.merchant.service.MerchantService;
import com.dufeng.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家商品管理接口（需 MERCHANT 角色）。
 */
@Tag(name = "商家-商品")
@RestController
@RequestMapping("/api/merchant/goods")
@RequiredArgsConstructor
public class MerchantGoodsController {

    private final GoodsService goodsService;
    private final MerchantService merchantService;

    private Long currentShopId() {
        return merchantService.getShopIdByAccountId(SecurityUtils.currentUserId());
    }

    @Operation(summary = "商品列表")
    @GetMapping
    public Result<PageResult<GoodsVO>> list(GoodsQuery query) {
        return Result.success(goodsService.merchantPage(currentShopId(), query));
    }

    @Operation(summary = "商品详情(含全部SKU)")
    @GetMapping("/{id}")
    public Result<GoodsDetailVO> detail(@PathVariable Long id) {
        return Result.success(goodsService.merchantDetail(currentShopId(), id));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody GoodsRequest request) {
        return Result.success(goodsService.createGoods(currentShopId(), request));
    }

    @Operation(summary = "编辑商品")
    @PutMapping("/{id}")
    public Result<Long> update(@PathVariable Long id, @Valid @RequestBody GoodsRequest request) {
        return Result.success(goodsService.updateGoods(currentShopId(), id, request));
    }

    @Operation(summary = "上架/下架")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        goodsService.changeMerchantGoodsStatus(currentShopId(), id, status);
        return Result.success();
    }
}
