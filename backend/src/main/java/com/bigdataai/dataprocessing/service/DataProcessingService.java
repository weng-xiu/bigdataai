package com.bigdataai.dataprocessing.service;

import java.util.List;
import java.util.Map;

/**
 * 数据处理服务接口
 * 负责数据的清洗、转换、聚合和分析等处理操作
 */
public interface DataProcessingService {
    
    /**
     * 数据清洗
     * @param dataSourceId 数据源ID
     * @param tableName 表名/集合名
     * @param rules 清洗规则
     * @return 处理结果
     */
    Map<String, Object> cleanData(Long dataSourceId, String tableName, Map<String, Object> rules);
    
    /**
     * 数据转换
     * @param dataSourceId 数据源ID
     * @param tableName 表名/集合名
     * @param transformations 转换规则
     * @return 处理结果
     */
    Map<String, Object> transformData(Long dataSourceId, String tableName, Map<String, Object> transformations);
    
    /**
     * 数据聚合
     * @param dataSourceId 数据源ID
     * @param tableName 表名/集合名
     * @param aggregations 聚合规则
     * @return 处理结果
     */
    Map<String, Object> aggregateData(Long dataSourceId, String tableName, Map<String, Object> aggregations);
    
    /**
     * 数据分析 - 分类
     * @param dataSourceId 数据源ID
     * @param tableName 表名/集合名
     * @param features 特征列
     * @param target 目标列
     * @param params 算法参数
     * @return 分析结果
     */
    Map<String, Object> classifyData(Long dataSourceId, String tableName, List<String> features, String target, Map<String, Object> params);
    
    /**
     * 数据分析 - 聚类
     * @param dataSourceId 数据源ID
     * @param tableName 表名/集合名
     * @param features 特征列
     * @param params 算法参数
     * @return 分析结果
     */
    Map<String, Object> clusterData(Long dataSourceId, String tableName, List<String> features, Map<String, Object> params);
    
    /**
     * 数据分析 - 关联规则挖掘
     * @param dataSourceId 数据源ID
     * @param tableName 表名/集合名
     * @param itemColumn 项目列
     * @param transactionColumn 事务列
     * @param params 算法参数
     * @return 分析结果
     */
    Map<String, Object> mineAssociationRules(Long dataSourceId, String tableName, String itemColumn, String transactionColumn, Map<String, Object> params);
    
    /**
     * 批处理任务提交
     * @param jobName 任务名称
     * @param jobType 任务类型
     * @param params 任务参数
     * @return 任务ID和状态
     */
    Map<String, Object> submitBatchJob(String jobName, String jobType, Map<String, Object> params);
    
    /**
     * 流处理任务提交
     * @param jobName 任务名称
     * @param sourceId 数据源ID
     * @param params 任务参数
     * @return 任务ID和状态
     */
    Map<String, Object> submitStreamingJob(String jobName, Long sourceId, Map<String, Object> params);
    
    /**
     * 获取任务状态
     * @param jobId 任务ID
     * @return 任务状态和进度
     */
    Map<String, Object> getJobStatus(String jobId);
    
    /**
     * 停止任务
     * @param jobId 任务ID
     * @return 操作结果
     */
    boolean stopJob(String jobId);
    
    /**
     * 获取任务结果
     * @param jobId 任务ID
     * @return 任务结果
     */
    Map<String, Object> getJobResult(String jobId);
}