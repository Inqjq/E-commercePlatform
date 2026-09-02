package com.dufeng.module.admin.service.impl;

import com.dufeng.module.admin.service.AdminService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.ResultCode;
import com.dufeng.module.admin.dto.DashboardVO;
import com.dufeng.module.admin.dto.GoodsAuditRequest;
import com.dufeng.module.admin.dto.MerchantPageQuery;
import com.dufeng.module.admin.dto.RoleRequest;
import com.dufeng.module.admin.dto.UserPageQuery;
import com.dufeng.module.admin.entity.Role;
import com.dufeng.module.admin.entity.RolePermission;
import com.dufeng.module.admin.mapper.RoleMapper;
import com.dufeng.module.admin.mapper.RolePermissionMapper;
import com.dufeng.module.goods.dto.GoodsQuery;
import com.dufeng.module.goods.dto.GoodsVO;
import com.dufeng.module.goods.entity.Goods;
import com.dufeng.module.goods.mapper.GoodsMapper;
import com.dufeng.module.goods.service.GoodsService;
import com.dufeng.module.merchant.dto.MerchantVO;
import com.dufeng.module.merchant.entity.Merchant;
import com.dufeng.module.merchant.mapper.MerchantMapper;
import com.dufeng.module.merchant.service.MerchantService;
import com.dufeng.module.order.entity.Orders;
import com.dufeng.module.order.mapper.OrdersMapper;
import com.dufeng.module.user.dto.UserVO;
import com.dufeng.module.user.entity.User;
import com.dufeng.module.user.mapper.UserMapper;
import com.dufeng.module.user.service.UserService;
import com.dufeng.security.AuthSessionService;
import com.dufeng.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台管理端聚合服务。
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantService merchantService;
    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;
    private final OrdersMapper ordersMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserService userService;
    private final AuthSessionService authSessionService;

    public PageResult<UserVO> pageUsers(UserPageQuery query) {
        Page<User> page = userMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()),
                new LambdaQueryWrapper<User>()
                        .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                        .and(StringUtils.hasText(query.getKeyword()), q -> q
                                .like(User::getUsername, query.getKeyword())
                                .or().like(User::getPhone, query.getKeyword())
                                .or().like(User::getNickname, query.getKeyword()))
                        .orderByDesc(User::getCreateTime));
        return PageResult.of(page, user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setGender(user.getGender());
            vo.setStatus(user.getStatus());
            vo.setRoles(userService.getRoles(user.getId()));
            return vo;
        });
    }

    public void updateUserStatus(Long userId, int status) {
        if (userId.equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能禁用当前登录账号");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status == 1 ? 1 : 0);
        userMapper.updateById(user);
        // 禁用后立即失效存量会话
        authSessionService.invalidateUser(userId);
    }

    public PageResult<MerchantVO> pageMerchants(MerchantPageQuery query) {
        Page<Merchant> page = merchantMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()),
                new LambdaQueryWrapper<Merchant>()
                        .eq(query.getAuditStatus() != null, Merchant::getAuditStatus, query.getAuditStatus())
                        .like(StringUtils.hasText(query.getKeyword()), Merchant::getName, query.getKeyword())
                        .orderByDesc(Merchant::getCreateTime));
        return PageResult.of(page, this::toMerchantVO);
    }

    public MerchantVO auditMerchant(Long merchantId, boolean approve, String reason) {
        return merchantService.audit(merchantId, approve, reason);
    }

    public PageResult<GoodsVO> pageGoods(GoodsQuery query) {
        return goodsService.pageQuery(query, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditGoods(Long goodsId, GoodsAuditRequest request) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        // 防止重复审核覆盖结果
        if (goods.getAuditStatus() != null && goods.getAuditStatus() >= 2) {
            throw new BusinessException(ResultCode.GOODS_STATUS_ERROR, "商品已完成审核，不可重复提交");
        }
        goods.setAuditStatus(request.getApprove() ? 2 : 3);
        goods.setStatus(request.getApprove() ? 2 : 4);
        goodsMapper.updateById(goods);
    }

    @Transactional(rollbackFor = Exception.class)
    public void forceOffline(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        goods.setStatus(3);
        goodsMapper.updateById(goods);
    }

    public List<Role> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByAsc(Role::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Role createRole(RoleRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        roleMapper.insert(role);
        if (request.getPermissionIds() != null) {
            for (Long permissionId : request.getPermissionIds()) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }
        return role;
    }

    public DashboardVO dashboard() {
        long userCount = userMapper.selectCount(null);
        long goodsCount = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 2));
        long orderCount = ordersMapper.selectCount(null);
        long merchantCount = merchantMapper.selectCount(null);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayOrderCount = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, todayStart));
        // 聚合下推到数据库，避免全量载入内存
        BigDecimal todaySales = ordersMapper.sumPaidAmountSince(todayStart);

        return DashboardVO.builder()
                .userCount(userCount)
                .goodsCount(goodsCount)
                .orderCount(orderCount)
                .todayOrderCount(todayOrderCount)
                .todaySales(todaySales)
                .merchantCount(merchantCount)
                .build();
    }

    private MerchantVO toMerchantVO(Merchant merchant) {
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
}
