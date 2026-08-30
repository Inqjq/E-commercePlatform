package com.dufeng.module.address.controller;

import com.dufeng.common.result.Result;
import com.dufeng.module.address.dto.AddressRequest;
import com.dufeng.module.address.entity.Address;
import com.dufeng.module.address.service.AddressService;
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
 * 前台收货地址接口。
 */
@Tag(name = "前台-收货地址")
@RestController
@RequestMapping("/api/portal/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "地址列表")
    @GetMapping
    public Result<List<Address>> list() {
        return Result.success(addressService.list(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "地址详情")
    @GetMapping("/{id}")
    public Result<Address> get(@PathVariable Long id) {
        return Result.success(addressService.get(SecurityUtils.currentUserId(), id));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result<Address> create(@Valid @RequestBody AddressRequest request) {
        return Result.success(addressService.create(SecurityUtils.currentUserId(), request));
    }

    @Operation(summary = "修改地址")
    @PutMapping("/{id}")
    public Result<Address> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return Result.success(addressService.update(SecurityUtils.currentUserId(), id, request));
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(SecurityUtils.currentUserId(), id);
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(SecurityUtils.currentUserId(), id);
        return Result.success();
    }
}
