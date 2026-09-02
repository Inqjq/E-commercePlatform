package com.dufeng.module.order.service.impl;

import com.dufeng.module.order.service.OrderService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.constant.BusinessMessages;
import com.dufeng.common.constant.CommonConstants;
import com.dufeng.common.constant.RedisKeyConstants;
import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.ResultCode;
import com.dufeng.config.OrderProperties;
import com.dufeng.module.address.entity.Address;
import com.dufeng.module.address.service.AddressService;
import com.dufeng.module.cart.dto.CartItemVO;
import com.dufeng.module.cart.dto.CartShopVO;
import com.dufeng.module.cart.service.CartService;
import com.dufeng.module.goods.entity.Goods;
import com.dufeng.module.goods.entity.Sku;
import com.dufeng.module.goods.mapper.SkuMapper;
import com.dufeng.module.goods.service.GoodsService;
import com.dufeng.module.merchant.mapper.ShopMapper;
import com.dufeng.module.order.constant.OrderStatus;
import com.dufeng.module.order.dto.OrderCreateRequest;
import com.dufeng.module.order.dto.OrderCreateResponse;
import com.dufeng.module.order.dto.OrderItemRequest;
import com.dufeng.module.order.dto.OrderItemVO;
import com.dufeng.module.order.dto.OrderQuery;
import com.dufeng.module.order.dto.OrderVO;
import com.dufeng.module.order.entity.OrderItem;
import com.dufeng.module.order.entity.Orders;
import com.dufeng.module.order.mapper.OrderItemMapper;
import com.dufeng.module.order.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单核心服务：创建（含拆单、库存预占、幂等）、查询、取消、确认收货。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 幂等键占位符，表示请求正在处理中。 */
    private static final String IDEMPOTENT_PENDING = "__PENDING__";

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final GoodsService goodsService;
    private final AddressService addressService;
    private final CartService cartService;
    private final ShopMapper shopMapper;
    private final StringRedisTemplate redisTemplate;
    private final OrderProperties orderProperties;

    @Transactional(rollbackFor = Exception.class)
    public List<OrderCreateResponse> createOrders(Long userId, OrderCreateRequest request) {
        // 幂等处理：原子占位，防止并发重复下单
        boolean idempotent = StringUtils.hasText(request.getRequestId());
        if (idempotent) {
            List<OrderCreateResponse> duplicated = claimIdempotent(userId, request.getRequestId());
            if (duplicated != null) {
                return duplicated;
            }
        }

        Address address = addressService.get(userId, request.getAddressId());
        Map<Long, List<OrderItemRequest>> shopGroup = resolveItems(userId, request);

        List<OrderCreateResponse> responses = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItemRequest>> entry : shopGroup.entrySet()) {
            Long shopId = entry.getKey();
            List<OrderItemRequest> items = entry.getValue();
            Orders order = createSingleOrder(userId, shopId, items, address, request.getRemark());
            responses.add(OrderCreateResponse.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .payAmount(order.getPayAmount())
                    .build());
            // 库存预占：单订单已扣减，重复造单需回补，此处按逻辑扣减
        }

        // 幂等记录
        if (idempotent) {
            rememberIdempotent(userId, request.getRequestId(), responses);
        }
        return responses;
    }

    public PageResult<OrderVO> pageQuery(Long userId, OrderQuery query) {
        Page<Orders> page = ordersMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()),
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .eq(query.getStatus() != null, Orders::getStatus, query.getStatus())
                        .orderByDesc(Orders::getCreateTime));
        return PageResult.of(page, this::toVO);
    }

    public OrderVO detail(Long userId, String orderNo) {
        Orders order = getByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        return toVO(order);
    }

    /**
     * 商家视角订单分页。
     */
    public PageResult<OrderVO> merchantPage(Long shopId, OrderQuery query) {
        Page<Orders> page = ordersMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()),
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getShopId, shopId)
                        .eq(query.getStatus() != null, Orders::getStatus, query.getStatus())
                        .orderByDesc(Orders::getCreateTime));
        return PageResult.of(page, this::toVO);
    }

    /**
     * 商家发货。
     */
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long shopId, String orderNo, String logisticsCompany, String logisticsNo) {
        Orders order = getByNo(orderNo);
        if (!order.getShopId().equals(shopId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (!Integer.valueOf(OrderStatus.WAIT_SHIP).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
        // CAS 流转：并发冲突（如同时取消/关单）时更新 0 行，抛异常回滚
        boolean updated = ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, order.getId())
                .eq(Orders::getStatus, OrderStatus.WAIT_SHIP)
                .set(Orders::getStatus, OrderStatus.WAIT_RECEIVE)
                .set(Orders::getShipTime, LocalDateTime.now())
                .set(Orders::getLogisticsCompany, logisticsCompany)
                .set(Orders::getLogisticsNo, logisticsNo)) > 0;
        if (!updated) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, String orderNo) {
        Orders order = getByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (!Integer.valueOf(OrderStatus.WAIT_PAY).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
        if (!transitionToCancelled(order, BusinessMessages.ORDER_CANCEL_BY_USER)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long userId, String orderNo) {
        Orders order = getByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (!Integer.valueOf(OrderStatus.WAIT_RECEIVE).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
        boolean updated = ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, order.getId())
                .eq(Orders::getStatus, OrderStatus.WAIT_RECEIVE)
                .set(Orders::getStatus, OrderStatus.COMPLETED)
                .set(Orders::getFinishTime, LocalDateTime.now())) > 0;
        if (!updated) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
        // 增加销量
        orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()))
                .forEach(item -> goodsService.increaseSales(item.getGoodsId(), item.getQuantity()));
    }

    /**
     * CAS 将订单置为已取消（仅当仍为待付款），成功后回补库存。
     * 供用户取消与超时关单复用；与支付回调并发时只有一方能完成迁移。
     *
     * @return true 表示本次调用完成了取消
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean transitionToCancelled(Orders order, String reason) {
        boolean updated = ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, order.getId())
                .eq(Orders::getStatus, OrderStatus.WAIT_PAY)
                .set(Orders::getStatus, OrderStatus.CANCELLED)
                .set(Orders::getCancelReason, reason)) > 0;
        if (updated) {
            restoreStock(order.getOrderNo());
        }
        return updated;
    }

    /**
     * CAS 将订单置为已支付（待付款→待发货），供支付回调调用。
     */
    public boolean tryMarkPaid(Long orderId) {
        return ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .eq(Orders::getStatus, OrderStatus.WAIT_PAY)
                .set(Orders::getStatus, OrderStatus.WAIT_SHIP)
                .set(Orders::getPayTime, LocalDateTime.now())) > 0;
    }

    public Orders getByNo(String orderNo) {
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    public Orders getById(Long id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 按商户拆分订单项。
     */
    private Map<Long, List<OrderItemRequest>> resolveItems(Long userId, OrderCreateRequest request) {
        List<OrderItemRequest> items = new ArrayList<>();
        if (Boolean.TRUE.equals(request.getFromCart())) {
            for (CartShopVO shopVO : cartService.list(userId)) {
                for (CartItemVO itemVO : shopVO.getItems()) {
                    if (Integer.valueOf(1).equals(itemVO.getChecked()) && Boolean.FALSE.equals(itemVO.getInvalid())) {
                        OrderItemRequest req = new OrderItemRequest();
                        req.setSkuId(itemVO.getSkuId());
                        req.setQuantity(itemVO.getQuantity());
                        items.add(req);
                    }
                }
            }
            if (items.isEmpty()) {
                throw new BusinessException(ResultCode.CART_EMPTY);
            }
        } else if (request.getItems() != null && !request.getItems().isEmpty()) {
            items = request.getItems();
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, BusinessMessages.ORDER_ITEMS_EMPTY);
        }

        Map<Long, List<OrderItemRequest>> grouped = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            Sku sku = goodsService.getSku(item.getSkuId());
            Long shopId = goodsService.getById(sku.getGoodsId()).getShopId();
            grouped.computeIfAbsent(shopId, k -> new ArrayList<>()).add(item);
        }
        return grouped;
    }

    private Orders createSingleOrder(Long userId, Long shopId, List<OrderItemRequest> items,
                                     Address address, String remark) {
        BigDecimal total = BigDecimal.ZERO;
        // 校验并扣减库存
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest req : items) {
            Sku sku = goodsService.getSku(req.getSkuId());
            Goods goods = goodsService.getById(sku.getGoodsId());
            if (!Integer.valueOf(2).equals(goods.getStatus())) {
                throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
            }
            int rows = skuMapper.deductStock(sku.getId(), req.getQuantity());
            if (rows == 0) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH,
                        String.format(BusinessMessages.GOODS_STOCK_NOT_ENOUGH, goods.getTitle()));
            }
            OrderItem item = new OrderItem();
            item.setShopId(shopId);
            item.setGoodsId(goods.getId());
            item.setSkuId(sku.getId());
            item.setGoodsTitle(goods.getTitle());
            item.setSpecText(sku.getSpecText());
            item.setImage(goods.getMainImage());
            item.setPrice(sku.getPrice());
            item.setQuantity(req.getQuantity());
            item.setTotalAmount(sku.getPrice().multiply(BigDecimal.valueOf(req.getQuantity())));
            item.setReviewed(0);
            orderItems.add(item);
            total = total.add(item.getTotalAmount());
        }

        Orders order = new Orders();
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setTotalAmount(total);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setPayAmount(total);
        order.setStatus(OrderStatus.WAIT_PAY);
        order.setReceiver(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setRemark(remark);
        order.setExpireTime(LocalDateTime.now().plusMinutes(orderProperties.getTimeoutMinutes()));
        ordersMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            item.setOrderNo(orderNo);
            orderItemMapper.insert(item);
        }
        // 清理购物车中已下单商品
        cartService.clearBySkuIds(userId, orderItems.stream().map(OrderItem::getSkuId).toList());
        return order;
    }

    private void restoreStock(String orderNo) {
        orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, orderNo))
                .forEach(item -> skuMapper.restoreStock(item.getSkuId(), item.getQuantity()));
    }

    private String generateOrderNo() {
        long ts = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(10000000, 99999999);
        return CommonConstants.ORDER_NO_PREFIX + ts + random;
    }

    /**
     * 原子占位幂等键：SETNX 首次请求写入占位符；重复请求按已完成结果复用或拒绝。
     */
    private List<OrderCreateResponse> claimIdempotent(Long userId, String requestId) {
        try {
            String key = idempotentKey(userId, requestId);
            Boolean claimed = redisTemplate.opsForValue().setIfAbsent(key, IDEMPOTENT_PENDING, Duration.ofMinutes(30));
            if (Boolean.TRUE.equals(claimed)) {
                return null;
            }
            String value = redisTemplate.opsForValue().get(key);
            if (value == null || IDEMPOTENT_PENDING.equals(value)) {
                // 占位失败且无已完成结果：上次处理中断或正在处理中，拒绝重复提交
                throw new BusinessException(ResultCode.DUPLICATE_REQUEST);
            }
            return buildDuplicated(value);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 不可用时降级放行，幂等由前端防抖与业务兜底
            log.warn("[Redis] 幂等占位失败，降级放行：{}", e.getMessage());
            return null;
        }
    }

    private List<OrderCreateResponse> buildDuplicated(String orderNos) {
        List<OrderCreateResponse> responses = new ArrayList<>();
        for (String orderNo : orderNos.split(",")) {
            Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderNo, orderNo));
            if (order != null) {
                responses.add(OrderCreateResponse.builder()
                        .id(order.getId())
                        .orderNo(order.getOrderNo())
                        .payAmount(order.getPayAmount())
                        .build());
            }
        }
        return responses;
    }

    private void rememberIdempotent(Long userId, String requestId, List<OrderCreateResponse> responses) {
        try {
            String orderNos = responses.stream().map(OrderCreateResponse::getOrderNo)
                    .collect(Collectors.joining(","));
            redisTemplate.opsForValue().set(idempotentKey(userId, requestId), orderNos, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.warn("[Redis] 幂等记录失败：{}", e.getMessage());
        }
    }

    private String idempotentKey(Long userId, String requestId) {
        return RedisKeyConstants.ORDER_IDEMPOTENT + userId + ":" + requestId;
    }

    private OrderVO toVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setShopId(order.getShopId());
        if (order.getShopId() != null) {
            com.dufeng.module.merchant.entity.Shop shop = shopMapper.selectById(order.getShopId());
            if (shop != null) {
                vo.setShopName(shop.getName());
            }
        }
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(OrderStatus.text(order.getStatus()));
        vo.setReceiver(order.getReceiver());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setExpireTime(order.getExpireTime());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setLogisticsCompany(order.getLogisticsCompany());
        vo.setLogisticsNo(order.getLogisticsNo());
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(this::toItemVO).toList());
        return vo;
    }

    private OrderItemVO toItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setGoodsId(item.getGoodsId());
        vo.setSkuId(item.getSkuId());
        vo.setGoodsTitle(item.getGoodsTitle());
        vo.setSpecText(item.getSpecText());
        vo.setImage(item.getImage());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setTotalAmount(item.getTotalAmount());
        vo.setReviewed(item.getReviewed());
        return vo;
    }
}
