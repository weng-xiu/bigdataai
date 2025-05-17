package com.bigdataai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;

/**
 * 大数据AI应用程序入口类
 * 排除了不必要的自动配置，保留了数据库相关配置
 * 设置为非Web应用类型，避免Web服务器相关配置
 */
@SpringBootApplication(exclude = {
    MongoDataAutoConfiguration.class,
    RedisAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    EurekaClientAutoConfiguration.class
})
public class BigDataApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BigDataApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE); // 设置为非Web应用
        application.run(args);
    }
}