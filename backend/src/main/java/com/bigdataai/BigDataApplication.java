package com.bigdataai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;

/**
 * 大数据AI应用程序入口类
 * 排除了不必要的自动配置，保留了数据库相关配置
 * 排除了Spark、Hadoop、Kafka、Elasticsearch等大数据组件的自动配置
 * 以避免启动时的冲突和错误
 */
@SpringBootApplication(exclude = {
    MongoDataAutoConfiguration.class,
    RedisAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    EurekaClientAutoConfiguration.class,
    KafkaAutoConfiguration.class,
    ElasticsearchRestClientAutoConfiguration.class
})
public class BigDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(BigDataApplication.class, args);
    }
}