package com.dufeng.module.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_role")
public class Role extends BaseEntity {

    private String name;

    private String code;

    private String description;
}
