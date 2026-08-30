package com.dufeng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单相关配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dufeng.order")
public class OrderProperties {

    /** 未支付订单自动关单时长（分钟）。 */
    private long timeoutMinutes = 30;
}
