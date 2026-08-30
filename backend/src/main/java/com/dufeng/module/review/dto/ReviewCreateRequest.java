package com.dufeng.module.review.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewCreateRequest {

    @NotNull(message = ValidationMessages.ORDER_ITEM_ID_NOT_NULL)
    private Long orderItemId;

    @NotNull(message = ValidationMessages.SCORE_NOT_NULL)
    @Min(value = 1, message = ValidationMessages.SCORE_MIN)
    @Max(value = 5, message = ValidationMessages.SCORE_MAX)
    private Integer score;

    @Size(max = 1024, message = ValidationMessages.REVIEW_CONTENT_SIZE)
    private String content;

    private String images;

    private Boolean anonymous;
}
