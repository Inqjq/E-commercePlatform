package com.dufeng.module.admin.service;

import com.dufeng.common.result.PageResult;
import com.dufeng.module.admin.dto.DashboardVO;
import com.dufeng.module.admin.dto.GoodsAuditRequest;
import com.dufeng.module.admin.dto.MerchantPageQuery;
import com.dufeng.module.admin.dto.RoleRequest;
import com.dufeng.module.admin.dto.UserPageQuery;
import com.dufeng.module.admin.entity.Role;
import com.dufeng.module.goods.dto.GoodsQuery;
import com.dufeng.module.goods.dto.GoodsVO;
import com.dufeng.module.goods.entity.Goods;
import com.dufeng.module.merchant.dto.MerchantVO;
import com.dufeng.module.merchant.entity.Merchant;
import com.dufeng.module.user.dto.UserVO;
import java.util.List;

public interface AdminService {

    PageResult<UserVO> pageUsers(UserPageQuery query);

    void updateUserStatus(Long userId, int status);

    PageResult<MerchantVO> pageMerchants(MerchantPageQuery query);

    MerchantVO auditMerchant(Long merchantId, boolean approve, String reason);

    PageResult<GoodsVO> pageGoods(GoodsQuery query);

    void auditGoods(Long goodsId, GoodsAuditRequest request);

    void forceOffline(Long goodsId);

    List<Role> listRoles();

    Role createRole(RoleRequest request);

    DashboardVO dashboard();

}
