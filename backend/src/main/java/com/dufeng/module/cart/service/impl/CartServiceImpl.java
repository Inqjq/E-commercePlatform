package com.dufeng.module.cart.service.impl;

import com.dufeng.module.cart.service.CartService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dufeng.common.constant.BusinessMessages;
import com.dufeng.common.constant.CommonConstants;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.module.cart.dto.CartItemVO;
import com.dufeng.module.cart.dto.CartShopVO;
import com.dufeng.module.cart.entity.CartItem;
import com.dufeng.module.cart.mapper.CartItemMapper;
import com.dufeng.module.goods.entity.Goods;
import com.dufeng.module.goods.entity.Sku;
import com.dufeng.module.goods.service.GoodsService;
import com.dufeng.module.merchant.entity.Shop;
import com.dufeng.module.merchant.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务，按店铺分组。
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final GoodsService goodsService;
    private final ShopMapper shopMapper;

    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, Long skuId, Integer quantity) {
        Sku sku = goodsService.getSku(skuId);
        if (!Integer.valueOf(1).equals(sku.getStatus())) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        if (sku.getStock() < quantity) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, skuId));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setChecked(1);
            cartItemMapper.updateById(existing);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setSkuId(skuId);
            item.setGoodsId(sku.getGoodsId());
            item.setShopId(goodsService.getById(sku.getGoodsId()).getShopId());
            item.setQuantity(quantity);
            item.setChecked(1);
            cartItemMapper.insert(item);
        }
    }

    public List<CartShopVO> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByAsc(CartItem::getCreateTime));
        Map<Long, List<CartItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(CartItem::getShopId));

        List<CartShopVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<CartItem>> entry : grouped.entrySet()) {
            Long shopId = entry.getKey();
            Shop shop = shopMapper.selectById(shopId);
            CartShopVO shopVO = new CartShopVO();
            shopVO.setShopId(shopId);
            shopVO.setShopName(shop == null ? CommonConstants.UNKNOWN_SHOP : shop.getName());
            BigDecimal checkedAmount = BigDecimal.ZERO;
            for (CartItem item : entry.getValue()) {
                CartItemVO itemVO = toVO(item);
                if (Boolean.TRUE.equals(itemVO.getChecked()) && Boolean.FALSE.equals(itemVO.getInvalid())) {
                    checkedAmount = checkedAmount.add(
                            itemVO.getPrice().multiply(BigDecimal.valueOf(itemVO.getQuantity())));
                }
                shopVO.getItems().add(itemVO);
            }
            shopVO.setCheckedAmount(checkedAmount);
            result.add(shopVO);
        }
        return result;
    }

    public void updateQuantity(Long userId, Long id, Integer quantity) {
        CartItem item = getOwned(userId, id);
        Sku sku = goodsService.getSku(item.getSkuId());
        if (sku.getStock() < quantity) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    public void setChecked(Long userId, Long id, boolean checked) {
        CartItem item = getOwned(userId, id);
        item.setChecked(checked ? 1 : 0);
        cartItemMapper.updateById(item);
    }

    public void remove(Long userId, Long id) {
        getOwned(userId, id);
        cartItemMapper.deleteById(id);
    }

    public void clearBySkuIds(Long userId, List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getSkuId, skuIds));
    }

    private CartItem getOwned(Long userId, Long id) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, BusinessMessages.CART_ITEM_NOT_FOUND);
        }
        return item;
    }

    private CartItemVO toVO(CartItem item) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setSkuId(item.getSkuId());
        vo.setGoodsId(item.getGoodsId());
        vo.setShopId(item.getShopId());
        vo.setQuantity(item.getQuantity());
        vo.setChecked(item.getChecked());
        vo.setInvalid(false);
        Goods goods = goodsService.getById(item.getGoodsId());
        vo.setGoodsTitle(goods.getTitle());
        vo.setGoodsImage(goods.getMainImage());
        Sku sku = goodsService.getSku(item.getSkuId());
        vo.setPrice(sku.getPrice());
        vo.setStock(sku.getStock());
        vo.setSpecText(sku.getSpecText());
        if (!Integer.valueOf(2).equals(goods.getStatus()) || sku.getStock() <= 0) {
            vo.setInvalid(true);
        }
        return vo;
    }
}
