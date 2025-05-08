package com.bigdataai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 任务调度配置类
 * 提供TaskScheduler Bean以支持定时任务和调度功能
 */
@Configuration
public class SchedulingConfig {

    /**
     * 创建并配置TaskScheduler Bean
     * 用于支持应用中的定时任务和调度功能
     * 
     * @return 配置好的ThreadPoolTaskScheduler实例
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // 设置线程池大小
        scheduler.setThreadNamePrefix("task-scheduler-"); // 设置线程名前缀
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 应用关闭时等待任务完成
        scheduler.setAwaitTerminationSeconds(60); // 等待终止的秒数
        return scheduler;
    }
}