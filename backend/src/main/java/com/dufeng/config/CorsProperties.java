package com.dufeng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨域配置。生产环境必须显式配置具体来源，禁止使用 *。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dufeng.cors")
public class CorsProperties {

    /** 允许的跨域来源列表；仅开发环境允许配置为 *。 */
    private List<String> allowedOrigins = new ArrayList<>();
}
