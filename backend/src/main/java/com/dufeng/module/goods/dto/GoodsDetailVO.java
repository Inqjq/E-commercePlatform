package com.dufeng.module.goods.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoodsDetailVO {

    private GoodsVO goods;
    private List<SkuVO> skus;
}
