package com.dufeng.module.merchant.service;

import com.dufeng.module.merchant.dto.MerchantApplyRequest;
import com.dufeng.module.merchant.dto.MerchantVO;
import com.dufeng.module.merchant.dto.ShopRequest;
import com.dufeng.module.merchant.dto.ShopVO;
import com.dufeng.module.merchant.entity.Merchant;
import com.dufeng.module.merchant.entity.Shop;

public interface MerchantService {

    MerchantVO apply(Long userId, MerchantApplyRequest request);

    MerchantVO getMyMerchant(Long userId);

    MerchantVO audit(Long merchantId, boolean approve, String reason);

    ShopVO getShopByAccountId(Long userId);

    ShopVO updateShop(Long userId, ShopRequest request);

    Long getShopIdByAccountId(Long userId);

    Merchant getMerchantByAccountId(Long userId);

}
