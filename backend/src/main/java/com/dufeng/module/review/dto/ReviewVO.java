package com.dufeng.module.review.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {

    private Long id;
    private Long orderItemId;
    private Long goodsId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer score;
    private String content;
    private String images;
    private Boolean anonymous;
    private LocalDateTime createTime;
}
