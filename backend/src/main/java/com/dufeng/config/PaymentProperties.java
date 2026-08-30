package com.dufeng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付相关配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dufeng.pay")
public class PaymentProperties {

    /** 回调签名密钥，生产环境必须通过环境变量注入，不提供默认值。 */
    private String secret;

    /** 模拟支付网关异步回调（发起支付后自动回查成功）。生产对接真实网关时必须关闭。 */
    private boolean mockCallbackEnabled = false;
}
