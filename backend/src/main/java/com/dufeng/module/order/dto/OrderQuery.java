package com.dufeng.module.order.dto;

import lombok.Data;

@Data
public class OrderQuery {

    private long current = 1;
    private long size = 10;
    private Integer status;
}
