package com.dufeng.module.goods.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dufeng.common.result.PageResult;
import com.dufeng.module.goods.dto.CategoryVO;
import com.dufeng.module.goods.dto.GoodsDetailVO;
import com.dufeng.module.goods.dto.GoodsQuery;
import com.dufeng.module.goods.dto.GoodsRequest;
import com.dufeng.module.goods.dto.GoodsVO;
import com.dufeng.module.goods.entity.Brand;
import com.dufeng.module.goods.entity.Goods;
import com.dufeng.module.goods.entity.Sku;
import java.util.List;

public interface GoodsService {

    PageResult<GoodsVO> pageQuery(GoodsQuery query, boolean onSaleOnly);

    GoodsDetailVO detail(Long id);

    List<CategoryVO> categoryTree();

    List<Brand> listBrands();

    List<GoodsVO> recommend();

    Long createGoods(Long shopId, GoodsRequest request);

    Long updateGoods(Long shopId, Long id, GoodsRequest request);

    PageResult<GoodsVO> merchantPage(Long shopId, GoodsQuery query);

    GoodsDetailVO merchantDetail(Long shopId, Long id);

    void changeMerchantGoodsStatus(Long shopId, Long id, Integer status);

    Goods getById(Long id);

    List<Sku> listEnabledSkus(Long goodsId);

    Sku getSku(Long skuId);

    void increaseSales(Long goodsId, Integer quantity);

    GoodsVO toVO(Goods goods);

}
