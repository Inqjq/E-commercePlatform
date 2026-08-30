package com.dufeng.module.goods.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class GoodsRequest {

    @NotNull(message = ValidationMessages.CATEGORY_NOT_NULL)
    private Long categoryId;

    private Long brandId;

    @NotBlank(message = ValidationMessages.GOODS_TITLE_NOT_BLANK)
    private String title;

    private String subtitle;
    private String mainImage;
    private String images;
    private String detail;
    private BigDecimal price;
    private List<SkuRequest> skus = new ArrayList<>();
}
