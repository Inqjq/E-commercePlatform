package com.dufeng.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardVO {

    private Long userCount;
    private Long goodsCount;
    private Long orderCount;
    private Long todayOrderCount;
    private BigDecimal todaySales;
    private Long merchantCount;
}
