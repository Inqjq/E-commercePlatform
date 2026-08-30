package com.dufeng.module.admin.dto;

import lombok.Data;

@Data
public class UserPageQuery {

    private long current = 1;
    private long size = 10;
    private String keyword;
    private Integer status;
}
