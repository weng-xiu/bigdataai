package com.bigdataai.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 应用程序配置类
 * 用于禁用不需要的自动配置，同时保留必要的数据库配置
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    MongoDataAutoConfiguration.class,
    RedisAutoConfiguration.class
})
public class ApplicationConfig {
    // 此配置类用于禁用不需要的自动配置
    // 同时保留MyBatis和数据库相关的配置
}