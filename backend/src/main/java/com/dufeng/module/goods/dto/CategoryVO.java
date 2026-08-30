package com.dufeng.module.goods.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryVO {

    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sort;
    private List<CategoryVO> children = new ArrayList<>();
}
