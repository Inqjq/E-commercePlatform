package com.dufeng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dufeng.auth")
public class AuthProperties {

    private long loginExpireSeconds = 604800;
    private long smsExpireSeconds = 300;
    private long smsSendIntervalSeconds = 60;
}
