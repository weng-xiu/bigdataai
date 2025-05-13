package com.bigdataai.config.minimal;

import org.springframework.context.annotation.Configuration;

/**
 * 最小化配置类
 * 用于在非Web应用模式下提供基本配置
 * 避免加载数据库和其他组件的配置
 */
@Configuration
public class MinimalConfig {
    // 此配置类为空，仅用于标识扫描包
    // 通过在BigDataApplication中指定scanBasePackages
    // 可以避免扫描到其他配置类，从而避免启动时的依赖问题
}