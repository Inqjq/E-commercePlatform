package com.dufeng.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局将 Long 序列化为字符串。
 *
 * <p>主键与关联 ID 由后端雪花算法生成，数值远超 JS Number.MAX_SAFE_INTEGER(2^53-1)，
 * 若以数字下发会被前端截断，导致按 ID 的详情/分类/品牌请求找不到记录。
 * 统一转字符串保证精度无损，前端按字符串透传即可。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance);
    }
}