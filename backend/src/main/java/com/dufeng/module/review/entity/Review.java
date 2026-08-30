package com.dufeng.module.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_review")
public class Review extends BaseEntity {

    private Long orderItemId;

    private Long orderId;

    private Long goodsId;

    private Long userId;

    /** 评分 1-5。 */
    private Integer score;

    private String content;

    private String images;

    /** 0 实名，1 匿名。 */
    private Integer anonymous;

    /** 0 待审核，1 公开，2 隐藏。 */
    private Integer status;
}
