package com.dufeng.module.goods.service.impl;

import com.dufeng.module.goods.service.GoodsService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.ResultCode;
import com.dufeng.module.goods.dto.CategoryVO;
import com.dufeng.module.goods.dto.GoodsDetailVO;
import com.dufeng.module.goods.dto.GoodsQuery;
import com.dufeng.module.goods.dto.GoodsRequest;
import com.dufeng.module.goods.dto.GoodsVO;
import com.dufeng.module.goods.dto.SkuRequest;
import com.dufeng.module.goods.dto.SkuVO;
import com.dufeng.module.goods.entity.Brand;
import com.dufeng.module.goods.entity.Category;
import com.dufeng.module.goods.entity.Goods;
import com.dufeng.module.goods.entity.Sku;
import com.dufeng.module.goods.mapper.BrandMapper;
import com.dufeng.module.goods.mapper.CategoryMapper;
import com.dufeng.module.goods.mapper.GoodsMapper;
import com.dufeng.module.goods.mapper.SkuMapper;
import com.dufeng.module.merchant.entity.Shop;
import com.dufeng.module.merchant.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品（SPU/SKU）浏览与管理。
 */
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final SkuMapper skuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final ShopMapper shopMapper;

    public PageResult<GoodsVO> pageQuery(GoodsQuery query, boolean onSaleOnly) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (onSaleOnly) {
            wrapper.eq(Goods::getStatus, 2);
        }
        if (query.getCategoryId() != null) {
            // 父类筛选包含其全部子分类，与前台类目树交互保持一致
            wrapper.in(Goods::getCategoryId, resolveCategoryIds(query.getCategoryId()));
        }
        wrapper.eq(query.getBrandId() != null, Goods::getBrandId, query.getBrandId())
                .eq(query.getShopId() != null, Goods::getShopId, query.getShopId())
                .and(StringUtils.hasText(query.getKeyword()), q -> q
                        .like(Goods::getTitle, query.getKeyword())
                          .or().like(Goods::getSubtitle, query.getKeyword()));
          if (query.getStatus() != null) {
              wrapper.eq(Goods::getStatus, query.getStatus());
          }
          if (query.getAuditStatus() != null) {
              wrapper.eq(Goods::getAuditStatus, query.getAuditStatus());
          }
          if (query.getPriceMin() != null) {
            wrapper.ge(Goods::getPrice, query.getPriceMin());
        }
        if (query.getPriceMax() != null) {
            wrapper.le(Goods::getPrice, query.getPriceMax());
        }
        applySort(wrapper, query.getSort());

        Page<Goods> page = goodsMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return PageResult.of(page, this::toVO);
    }

    /**
     * 解析目标分类自身及其所有子孙分类的 id，用于父类筛选时命中子类商品。
     */
    private Set<Long> resolveCategoryIds(Long categoryId) {
        List<Category> all = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1));
        Map<Long, List<Long>> childrenMap = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId(),
                        Collectors.mapping(Category::getId, Collectors.toList())));
        Set<Long> result = new HashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(categoryId);
        while (!stack.isEmpty()) {
            Long current = stack.pop();
            if (result.add(current)) {
                stack.addAll(childrenMap.getOrDefault(current, List.of()));
            }
        }
        return result;
    }

    public GoodsDetailVO detail(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || !Integer.valueOf(2).equals(goods.getStatus())) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getGoodsId, id)
                .eq(Sku::getStatus, 1)
                .orderByAsc(Sku::getId));
        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setGoods(toVO(goods));
        vo.setSkus(skus.stream().map(this::toSkuVO).toList());
        return vo;
    }

    public List<CategoryVO> categoryTree() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
        Map<Long, List<Category>> grouped = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        return buildTree(0L, grouped);
    }

    public List<Brand> listBrands() {
        return brandMapper.selectList(new LambdaQueryWrapper<Brand>().orderByAsc(Brand::getId));
    }

    public List<GoodsVO> recommend() {
        List<Goods> goods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 2)
                .orderByDesc(Goods::getSales)
                .last("LIMIT 10"));
        return goods.stream().map(this::toVO).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createGoods(Long shopId, GoodsRequest request) {
        Goods goods = new Goods();
        fillGoods(goods, shopId, request);
        // 默认提交审核，审核通过后上架。
        goods.setStatus(1);
        goods.setAuditStatus(1);
        goodsMapper.insert(goods);
        saveSkus(goods.getId(), request.getSkus());
        return goods.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long updateGoods(Long shopId, Long id, GoodsRequest request) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        if (!goods.getShopId().equals(shopId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        fillGoods(goods, shopId, request);
        goods.setStatus(1);
        goods.setAuditStatus(1);
        goodsMapper.updateById(goods);

        syncSkus(id, request.getSkus());
        return id;
    }

    public PageResult<GoodsVO> merchantPage(Long shopId, GoodsQuery query) {
        query.setShopId(shopId);
        return pageQuery(query, false);
    }

    /** 商家商品详情：校验归属，返回全部 SKU（编辑页回填用）。 */
    public GoodsDetailVO merchantDetail(Long shopId, Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        if (!goods.getShopId().equals(shopId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getGoodsId, id)
                .orderByAsc(Sku::getId));
        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setGoods(toVO(goods));
        vo.setSkus(skus.stream().map(this::toSkuVO).toList());
        return vo;
    }

    /** 商家上架/下架：仅允许在售(2)与下架(3)互转，且只能操作自己的商品。 */
    public void changeMerchantGoodsStatus(Long shopId, Long id, Integer status) {
        if (!Integer.valueOf(2).equals(status) && !Integer.valueOf(3).equals(status)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持上架或下架操作");
        }
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        if (!goods.getShopId().equals(shopId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        goods.setStatus(status);
        goodsMapper.updateById(goods);
    }

    public Goods getById(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        return goods;
    }

    public List<Sku> listEnabledSkus(Long goodsId) {
        return skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getGoodsId, goodsId)
                .eq(Sku::getStatus, 1));
    }

    public Sku getSku(Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }
        return sku;
    }

    public void increaseSales(Long goodsId, Integer quantity) {
        goodsMapper.increaseSales(goodsId, quantity == null ? 0 : quantity);
    }

    public GoodsVO toVO(Goods goods) {
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(goods, vo);
        if (goods.getShopId() != null) {
            Shop shop = shopMapper.selectById(goods.getShopId());
            if (shop != null) {
                vo.setShopName(shop.getName());
            }
        }
        if (goods.getBrandId() != null) {
            var brand = brandMapper.selectById(goods.getBrandId());
            if (brand != null) {
                vo.setBrandName(brand.getName());
            }
        }
        return vo;
    }

    private SkuVO toSkuVO(Sku sku) {
        SkuVO vo = new SkuVO();
        BeanUtils.copyProperties(sku, vo);
        return vo;
    }

    private void fillGoods(Goods goods, Long shopId, GoodsRequest request) {
        goods.setShopId(shopId);
        goods.setCategoryId(request.getCategoryId());
        goods.setBrandId(request.getBrandId());
        goods.setTitle(request.getTitle());
        goods.setSubtitle(request.getSubtitle());
        goods.setMainImage(request.getMainImage());
        goods.setImages(request.getImages());
        goods.setDetail(request.getDetail());
        BigDecimal minPrice = request.getSkus().stream()
                .map(SkuRequest::getPrice)
                .filter(java.util.Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(request.getPrice() == null ? BigDecimal.ZERO : request.getPrice());
        goods.setPrice(minPrice);
    }

    private void saveSkus(Long goodsId, List<SkuRequest> skuRequests) {
        for (SkuRequest req : skuRequests) {
            Sku sku = new Sku();
            sku.setGoodsId(goodsId);
            fillSku(sku, req);
            sku.setStatus(1);
            skuMapper.insert(sku);
        }
    }

    /**
     * 编辑商品时增量同步 SKU：已有规格原地更新（保留主键，订单/购物车的 SKU 引用不受影响），
     * 新规格插入，请求中未携带的旧规格下架（禁用而非物理删除，保证历史订单取消时仍能回补库存）。
     * 请求未携带 skus 时视为不修改规格，避免误下架全部 SKU。
     */
    private void syncSkus(Long goodsId, List<SkuRequest> skuRequests) {
        if (skuRequests == null || skuRequests.isEmpty()) {
            return;
        }
        List<Sku> existing = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getGoodsId, goodsId));
        Map<Long, Sku> existingById = existing.stream().collect(Collectors.toMap(Sku::getId, s -> s));

        Set<Long> keptIds = new HashSet<>();
        for (SkuRequest req : skuRequests) {
            Sku current = req.getId() != null ? existingById.get(req.getId()) : null;
            if (current != null) {
                Sku patch = new Sku();
                patch.setId(current.getId());
                fillSku(patch, req);
                patch.setStatus(1);
                skuMapper.updateById(patch);
                // 库存按差量原子调整，避免覆盖并发下单产生的扣减
                int delta = (req.getStock() == null ? 0 : req.getStock())
                        - (current.getStock() == null ? 0 : current.getStock());
                if (delta != 0) {
                    skuMapper.adjustStock(current.getId(), delta);
                }
                keptIds.add(current.getId());
            } else {
                saveSkus(goodsId, List.of(req));
            }
        }
        for (Sku sku : existing) {
            if (!keptIds.contains(sku.getId()) && Integer.valueOf(1).equals(sku.getStatus())) {
                Sku disable = new Sku();
                disable.setId(sku.getId());
                disable.setStatus(0);
                skuMapper.updateById(disable);
            }
        }
    }

    private void fillSku(Sku sku, SkuRequest req) {
        sku.setSkuCode(req.getSkuCode());
        sku.setSpecJson(req.getSpecJson());
        sku.setSpecText(req.getSpecText());
        sku.setImage(req.getImage());
        sku.setPrice(req.getPrice());
        sku.setWeight(req.getWeight() == null ? BigDecimal.ZERO : req.getWeight());
    }

    private void applySort(LambdaQueryWrapper<Goods> wrapper, String sort) {
        if ("sales".equals(sort)) {
            wrapper.orderByDesc(Goods::getSales);
        } else if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Goods::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Goods::getPrice);
        } else {
            wrapper.orderByDesc(Goods::getCreateTime);
        }
    }

    private List<CategoryVO> buildTree(Long parentId, Map<Long, List<Category>> grouped) {
        List<Category> children = grouped.getOrDefault(parentId, new ArrayList<>());
        List<CategoryVO> result = new ArrayList<>();
        for (Category category : children) {
            CategoryVO vo = new CategoryVO();
            vo.setId(category.getId());
            vo.setParentId(category.getParentId());
            vo.setName(category.getName());
            vo.setLevel(category.getLevel());
            vo.setSort(category.getSort());
            vo.setChildren(buildTree(category.getId(), grouped));
            result.add(vo);
        }
        return result;
    }
}
