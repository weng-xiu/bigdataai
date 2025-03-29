package com.bigdataai.dataintegration.service;

import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.model.DataSourceType;

import java.util.List;
import java.util.Map;

/**
 * 数据源服务接口
 */
public interface DataSourceService {
    
    /**
     * 创建数据源
     * 
     * @param dataSource 数据源对象
     * @return 创建后的数据源对象
     */
    DataSource createDataSource(DataSource dataSource);
    
    /**
     * 更新数据源
     * 
     * @param dataSource 数据源对象
     * @return 更新后的数据源对象
     */
    DataSource updateDataSource(DataSource dataSource);
    
    /**
     * 根据ID获取数据源
     * 
     * @param id 数据源ID
     * @return 数据源对象
     */
    DataSource getDataSource(Long id);
    
    /**
     * 根据名称获取数据源
     * 
     * @param name 数据源名称
     * @return 数据源对象
     */
    DataSource getDataSourceByName(String name);
    
    /**
     * 获取所有数据源
     * 
     * @return 数据源列表
     */
    List<DataSource> getAllDataSources();
    
    /**
     * 根据类型获取数据源列表
     * 
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<DataSource> getDataSourcesByType(DataSourceType type);
    
    /**
     * 删除数据源
     * 
     * @param id 数据源ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteDataSource(Long id);
    
    /**
     * 测试数据源连接
     * 
     * @param dataSource 数据源对象
     * @return 测试结果，包含success和message字段
     */
    Map<String, Object> testConnection(DataSource dataSource);
    
    /**
     * 获取数据源的表/集合列表
     * 
     * @param dataSourceId 数据源ID
     * @return 表/集合名称列表
     */
    List<String> getTableList(Long dataSourceId);
    
    /**
     * 获取表/集合的结构信息
     * 
     * @param dataSourceId 数据源ID
     * @param tableName 表/集合名称
     * @return 表/集合结构信息
     */
    List<Map<String, Object>> getTableSchema(Long dataSourceId, String tableName);
}