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
    /**
     * 创建 SparkConf Bean。
     * 从配置文件中读取Spark配置，并提供默认值以防配置缺失
     * @return SparkConf 实例
     */
    @Bean
    public SparkConf sparkConf() {
        // 从 Environment 获取配置，并提供默认值
        String master = env.getProperty("spark.master", "local[*]");
        String appName = env.getProperty("spark.app-name", "BigDataProcessing"); // 注意属性名是 app-name
        String executorMemory = env.getProperty("spark.executor.memory", "1g");
        String driverMemory = env.getProperty("spark.driver.memory", "1g");

        // 创建并返回SparkConf实例
        return new SparkConf()
                .setMaster(master)
                .setAppName(appName)
                .set("spark.executor.memory", executorMemory)
                .set("spark.driver.memory", driverMemory);
    }

    /**
     * 创建 SparkSession Bean。
     * 使用配置好的SparkConf创建SparkSession实例
     * 
     * @param sparkConf 已配置的SparkConf实例
     * @return SparkSession 实例
     */
    @Bean
    public SparkSession sparkSession(SparkConf sparkConf) {
        return SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }

    /**
     * 创建 JavaSparkContext Bean。
     * 使用配置好的SparkConf创建JavaSparkContext实例
     * 
     * @param sparkConf 已配置的SparkConf实例
     * @return JavaSparkContext 实例
     */
    @Bean
    public JavaSparkContext javaSparkContext(SparkConf sparkConf) {
        return new JavaSparkContext(sparkConf);
    }
}