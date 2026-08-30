package com.dufeng.module.admin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理端操作审计注解，标注在需要留痕的接口方法上。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminAudit {

    /** 业务模块，如"用户管理"。 */
    String module();

    /** 操作动作，如"禁用用户"。 */
    String action();
}
