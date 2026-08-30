package com.dufeng.config;

import com.dufeng.common.constant.SecurityConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 与认证相关配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dufeng.jwt")
public class JwtProperties {

    /** Base64 编码的 HMAC 密钥。 */
    private String secret;

    /** Token 有效期（秒），默认 7 天。 */
    private long expireSeconds = 604800;

    /** 请求头名称。 */
    private String header = SecurityConstants.AUTH_HEADER;

    /** Token 前缀。 */
    private String prefix = SecurityConstants.BEARER_PREFIX;
}
