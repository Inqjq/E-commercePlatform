package com.dufeng.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dufeng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String username;

    private String passwordHash;

    private String phone;

    private String email;

    private String nickname;

    private String avatar;

    private Integer gender;

    /** 0 禁用，1 启用。 */
    private Integer status;
}
