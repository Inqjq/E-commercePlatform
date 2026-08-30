package com.dufeng.module.goods.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.goods.dto.CategoryVO;
import com.dufeng.module.goods.dto.GoodsDetailVO;
import com.dufeng.module.goods.dto.GoodsQuery;
import com.dufeng.module.goods.dto.GoodsVO;
import com.dufeng.module.goods.entity.Brand;
import com.dufeng.module.goods.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台商品浏览接口（公开）。
 */
@Tag(name = "前台-商品")
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalGoodsController {

    private final GoodsService goodsService;

    @Operation(summary = "商品列表")
    @GetMapping("/goods/list")
    public Result<PageResult<GoodsVO>> list(GoodsQuery query) {
        return Result.success(goodsService.pageQuery(query, true));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/goods/{id}")
    public Result<GoodsDetailVO> detail(@PathVariable Long id) {
        return Result.success(goodsService.detail(id));
    }

    @Operation(summary = "类目树")
    @GetMapping("/category/tree")
    public Result<List<CategoryVO>> categoryTree() {
        return Result.success(goodsService.categoryTree());
    }

    @Operation(summary = "品牌列表")
    @GetMapping("/brand/list")
    public Result<List<Brand>> brandList() {
        return Result.success(goodsService.listBrands());
    }

    @Operation(summary = "首页推荐")
    @GetMapping("/home/recommend")
    public Result<List<GoodsVO>> recommend() {
        return Result.success(goodsService.recommend());
    }
}
