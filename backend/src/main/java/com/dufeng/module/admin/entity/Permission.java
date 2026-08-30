package com.dufeng.module.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_permission")
public class Permission extends BaseEntity {

    private Long parentId;

    private String name;

    private String code;

    /** 1 菜单，2 按钮，3 数据。 */
    private Integer type;

    private String route;

    private Integer sort;

    private Integer status;
}
