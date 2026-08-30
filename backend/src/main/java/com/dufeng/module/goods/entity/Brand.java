package com.dufeng.module.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_brand")
public class Brand extends BaseEntity {

    private String name;

    private String logo;

    private String description;
}
