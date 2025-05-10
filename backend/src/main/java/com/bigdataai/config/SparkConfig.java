package com.bigdataai.config;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spark配置类
 * 只有当spark.enabled=true时才会初始化Spark相关组件
 */
@Configuration
@ConditionalOnProperty(name = "spark.enabled", havingValue = "true", matchIfMissing = false)
public class SparkConfig {

    @Autowired
    private Environment env;

    /**
     * 创建 SparkConf Bean。
     * @return SparkConf 实例
     */
    @Bean
    public SparkConf sparkConf() {
        // 从 Environment 获取配置
        String master = env.getProperty("spark.master");
        String appName = env.getProperty("spark.app-name"); // 注意属性名是 app-name
        String executorMemory = env.getProperty("spark.executor.memory");
        String driverMemory = env.getProperty("spark.driver.memory");

        return new SparkConf()
                .setMaster(master)
                .setAppName(appName)
                .set("spark.executor.memory", executorMemory)
                .set("spark.driver.memory", driverMemory);
    }

    @Bean
    public SparkSession sparkSession(SparkConf sparkConf) {
        return SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }

    @Bean
    public JavaSparkContext javaSparkContext(SparkConf sparkConf) {
        return new JavaSparkContext(sparkConf);
    }
}