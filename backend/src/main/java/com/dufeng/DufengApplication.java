package com.dufeng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 渡风电商平台后端启动入口。
 */
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.dufeng.module", annotationClass = org.apache.ibatis.annotations.Mapper.class)
public class DufengApplication {

    public static void main(String[] args) {
        SpringApplication.run(DufengApplication.class, args);
    }
}
