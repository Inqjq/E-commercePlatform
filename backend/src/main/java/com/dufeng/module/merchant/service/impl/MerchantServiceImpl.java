package com.dufeng.module.merchant.service.impl;

import com.dufeng.module.merchant.service.MerchantService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dufeng.common.constant.SecurityConstants;
import com.dufeng.common.constant.BusinessMessages;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.module.merchant.dto.MerchantApplyRequest;
import com.dufeng.module.merchant.dto.MerchantVO;
import com.dufeng.module.merchant.dto.ShopRequest;
import com.dufeng.module.merchant.dto.ShopVO;
import com.dufeng.module.merchant.entity.Merchant;
import com.dufeng.module.merchant.entity.Shop;
import com.dufeng.module.merchant.mapper.MerchantMapper;
import com.dufeng.module.merchant.mapper.ShopMapper;
import com.dufeng.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家入驻与店铺管理。
 */
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final ShopMapper shopMapper;
    private final UserService userService;

    @Transactional(rollbackFor = Exception.class)
    public MerchantVO apply(Long userId, MerchantApplyRequest request) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAccountId, userId));
        if (merchant == null) {
            merchant = new Merchant();
            merchant.setAccountId(userId);
            copyApply(request, merchant);
            merchant.setAuditStatus(0);
            merchant.setStatus(1);
            merchantMapper.insert(merchant);
        } else {
            if (Integer.valueOf(1).equals(merchant.getAuditStatus())) {
                throw new BusinessException(ResultCode.MERCHANT_AUDIT_INVALID, BusinessMessages.MERCHANT_ALREADY_APPROVED);
            }
            copyApply(request, merchant);
            merchant.setAuditStatus(0);
            merchant.setAuditReason(null);
            merchantMapper.updateById(merchant);
        }
        return toVO(merchant);
    }

    public MerchantVO getMyMerchant(Long userId) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAccountId, userId));
        if (merchant == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }
        return toVO(merchant);
    }

    @Transactional(rollbackFor = Exception.class)
    public MerchantVO audit(Long merchantId, boolean approve, String reason) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }
        // 防止重复审核
        if (Integer.valueOf(1).equals(merchant.getAuditStatus())
                || Integer.valueOf(2).equals(merchant.getAuditStatus())) {
            throw new BusinessException(ResultCode.MERCHANT_AUDIT_INVALID, BusinessMessages.MERCHANT_ALREADY_APPROVED);
        }
        if (approve) {
            merchant.setAuditStatus(1);
            merchant.setAuditReason(null);
            // 创建店铺
            Shop existing = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                    .eq(Shop::getMerchantId, merchant.getId()));
            if (existing == null) {
                Shop shop = new Shop();
                shop.setMerchantId(merchant.getId());
                shop.setName(merchant.getName());
                shop.setStatus(1);
                shopMapper.insert(shop);
            }
            // 授予商家角色
            userService.assignRole(merchant.getAccountId(), SecurityConstants.ROLE_MERCHANT);
        } else {
            merchant.setAuditStatus(2);
            merchant.setAuditReason(reason != null ? reason : "资料不符合要求");
        }
        merchantMapper.updateById(merchant);
        return toVO(merchant);
    }

    public ShopVO getShopByAccountId(Long userId) {
        Merchant merchant = getMerchantByAccountId(userId);
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchant.getId()));
        if (shop == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND, BusinessMessages.SHOP_NOT_OPEN);
        }
        return toShopVO(shop);
    }

    @Transactional(rollbackFor = Exception.class)
    public ShopVO updateShop(Long userId, ShopRequest request) {
        Merchant merchant = getMerchantByAccountId(userId);
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchant.getId()));
        if (shop == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND, BusinessMessages.SHOP_NOT_OPEN);
        }
        shop.setName(request.getName());
        shop.setLogo(request.getLogo());
        shop.setIntro(request.getIntro());
        shop.setServicePhone(request.getServicePhone());
        shopMapper.updateById(shop);
        return toShopVO(shop);
    }

    public Long getShopIdByAccountId(Long userId) {
        return getShopByAccountId(userId).getId();
    }

    public Merchant getMerchantByAccountId(Long userId) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAccountId, userId));
        if (merchant == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }
        return merchant;
    }

    private void copyApply(MerchantApplyRequest request, Merchant merchant) {
        merchant.setName(request.getName());
        merchant.setLicenseNo(request.getLicenseNo());
        merchant.setLegalPerson(request.getLegalPerson());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setCategoryIds(request.getCategoryIds());
    }

    private MerchantVO toVO(Merchant merchant) {
        MerchantVO vo = new MerchantVO();
        vo.setId(merchant.getId());
        vo.setAccountId(merchant.getAccountId());
        vo.setName(merchant.getName());
        vo.setLicenseNo(merchant.getLicenseNo());
        vo.setLegalPerson(merchant.getLegalPerson());
        vo.setContactPhone(merchant.getContactPhone());
        vo.setCategoryIds(merchant.getCategoryIds());
        vo.setAuditStatus(merchant.getAuditStatus());
        vo.setAuditReason(merchant.getAuditReason());
        vo.setStatus(merchant.getStatus());
        vo.setCreateTime(merchant.getCreateTime());
        return vo;
    }

    private ShopVO toShopVO(Shop shop) {
        ShopVO vo = new ShopVO();
        vo.setId(shop.getId());
        vo.setMerchantId(shop.getMerchantId());
        vo.setName(shop.getName());
        vo.setLogo(shop.getLogo());
        vo.setIntro(shop.getIntro());
        vo.setServicePhone(shop.getServicePhone());
        vo.setStatus(shop.getStatus());
        return vo;
    }
}
