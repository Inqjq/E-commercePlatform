package com.dufeng.module.review.service.impl;

import com.dufeng.module.review.service.ReviewService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dufeng.common.constant.BusinessMessages;
import com.dufeng.common.constant.CommonConstants;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.PageResult;
import com.dufeng.common.result.ResultCode;
import com.dufeng.module.order.constant.OrderStatus;
import com.dufeng.module.order.entity.OrderItem;
import com.dufeng.module.order.entity.Orders;
import com.dufeng.module.order.mapper.OrderItemMapper;
import com.dufeng.module.order.service.OrderService;
import com.dufeng.module.review.dto.ReviewCreateRequest;
import com.dufeng.module.review.dto.ReviewVO;
import com.dufeng.module.review.entity.Review;
import com.dufeng.module.review.mapper.ReviewMapper;
import com.dufeng.module.user.entity.User;
import com.dufeng.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评价服务。
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public void create(Long userId, ReviewCreateRequest request) {
        OrderItem item = orderItemMapper.selectById(request.getOrderItemId());
        if (item == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        Orders order = orderService.getById(item.getOrderId());
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (!Integer.valueOf(OrderStatus.COMPLETED).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, BusinessMessages.REVIEW_ONLY_COMPLETED);
        }
        if (Integer.valueOf(1).equals(item.getReviewed())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, BusinessMessages.REVIEW_ALREADY_SUBMITTED);
        }

        Review review = new Review();
        review.setOrderItemId(item.getId());
        review.setOrderId(item.getOrderId());
        review.setGoodsId(item.getGoodsId());
        review.setUserId(userId);
        review.setScore(request.getScore());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        review.setAnonymous(Boolean.TRUE.equals(request.getAnonymous()) ? 1 : 0);
        review.setStatus(1);
        reviewMapper.insert(review);

        item.setReviewed(1);
        orderItemMapper.updateById(item);
    }

    public PageResult<ReviewVO> pageByGoods(Long goodsId, long current, long size) {
        Page<Review> page = reviewMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getGoodsId, goodsId)
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getCreateTime));
        return PageResult.of(page, this::toVO);
    }

    private ReviewVO toVO(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderItemId(review.getOrderItemId());
        vo.setGoodsId(review.getGoodsId());
        vo.setUserId(review.getUserId());
        vo.setScore(review.getScore());
        vo.setContent(review.getContent());
        vo.setImages(review.getImages());
        vo.setAnonymous(Integer.valueOf(1).equals(review.getAnonymous()));
        vo.setCreateTime(review.getCreateTime());
        if (Boolean.TRUE.equals(vo.getAnonymous())) {
            vo.setNickname(CommonConstants.ANONYMOUS_USER);
        } else {
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
        }
        return vo;
    }
}
