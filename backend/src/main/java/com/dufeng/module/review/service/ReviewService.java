package com.dufeng.module.review.service;

import com.dufeng.common.result.PageResult;
import com.dufeng.module.review.dto.ReviewCreateRequest;
import com.dufeng.module.review.dto.ReviewVO;

public interface ReviewService {

    void create(Long userId, ReviewCreateRequest request);

    PageResult<ReviewVO> pageByGoods(Long goodsId, long current, long size);

}
