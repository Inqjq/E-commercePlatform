package com.dufeng.module.review.controller;

import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.Result;
import com.dufeng.module.review.dto.ReviewCreateRequest;
import com.dufeng.module.review.dto.ReviewVO;
import com.dufeng.module.review.service.ReviewService;
import com.dufeng.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台评价接口。
 */
@Tag(name = "前台-评价")
@RestController
@RequestMapping("/api/portal/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "对订单商品发表评价")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReviewCreateRequest request) {
        reviewService.create(SecurityUtils.currentUserId(), request);
        return Result.success();
    }

    @Operation(summary = "商品评价列表")
    @GetMapping
    public Result<PageResult<ReviewVO>> list(@RequestParam Long goodsId,
                                             @RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.success(reviewService.pageByGoods(goodsId, current, size));
    }
}
