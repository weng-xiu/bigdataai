package com.bigdataai.config;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spark配置类
 */
@Configuration
public class SparkConfig {

    @Value("${spark.master}")
    private String master;

    @Value("${spark.app.name}")
    private String appName;

    @Value("${spark.executor.memory}")
    private String executorMemory;

    @Value("${spark.driver.memory}")
    private String driverMemory;

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf()
                .setMaster(master)
                .setAppName(appName)
                .set("spark.executor.memory", executorMemory)
                .set("spark.driver.memory", driverMemory)
                .set("spark.driver.extraLibraryPath", System.getenv("HADOOP_HOME") + "/bin")
                .set("spark.executor.extraLibraryPath", System.getenv("HADOOP_HOME") + "/bin");
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