package com.dufeng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝开放平台配置（沙箱/生产共用，默认关闭走模拟支付）。
 * <p>沙箱网关：https://openapi-sandbox.dl.alipaydev.com/gateway.do
 * 生产网关：https://openapi.alipay.com/gateway.do</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "dufeng.alipay")
public class AlipayProperties {

    /** 是否启用支付宝真实支付（沙箱或生产），关闭时 alipay 渠道退回模拟回调。 */
    private boolean enabled = false;

    /** 开放平台 APPID（沙箱应用即沙箱 APPID）。 */
    private String appId;

    /** 应用私钥（PKCS8，开放平台密钥工具生成）。 */
    private String privateKey;

    /** 支付宝公钥（用于验签异步通知，注意不是应用公钥）。 */
    private String alipayPublicKey;

    /** 支付宝网关地址。 */
    private String gateway = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private String signType = "RSA2";

    private String charset = "UTF-8";

    private String format = "json";

    /** 异步通知地址，必须是公网可访问的完整 URL（沙箱同样要求）。 */
    private String notifyUrl;

    /** 支付完成后浏览器跳转地址（填前端站点地址即可，非支付结果依据）。 */
    private String returnUrl;
}
