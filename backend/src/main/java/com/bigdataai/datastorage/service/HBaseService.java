package com.bigdataai.datastorage.service;

import java.util.List;
import java.util.Map;

/**
 * HBase存储服务接口
 * 负责HBase数据库的存储操作
 */
public interface HBaseService {
    
    /**
     * 创建表
     * @param tableName 表名
     * @param columnFamilies 列族数组
     * @return 是否创建成功
     */
    boolean createTable(String tableName, String[] columnFamilies);
    
    /**
     * 删除表
     * @param tableName 表名
     * @return 是否删除成功
     */
    boolean deleteTable(String tableName);
    
    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return 是否存在
     */
    boolean tableExists(String tableName);
    
    /**
     * 列出所有表
     * @return 表名列表
     */
    List<String> listTables();
    
    /**
     * 获取表描述信息
     * @param tableName 表名
     * @return 表描述信息
     */
    Map<String, Object> getTableDescription(String tableName);
    
    /**
     * 添加列族
     * @param tableName 表名
     * @param columnFamily 列族名
     * @return 是否添加成功
     */
    boolean addColumnFamily(String tableName, String columnFamily);
    
    /**
     * 删除列族
     * @param tableName 表名
     * @param columnFamily 列族名
     * @return 是否删除成功
     */
    boolean deleteColumnFamily(String tableName, String columnFamily);
    
    /**
     * 插入一行数据
     * @param tableName 表名
     * @param rowKey 行键
     * @param columnFamily 列族名
     * @param column 列名
     * @param value 值
     * @return 是否插入成功
     */
    boolean putRow(String tableName, String rowKey, String columnFamily, String column, String value);
    
    /**
     * 批量插入数据
     * @param tableName 表名
     * @param rows 行数据列表
     * @return 是否插入成功
     */
    boolean putRows(String tableName, List<Map<String, Object>> rows);
    
    /**
     * 获取一行数据
     * @param tableName 表名
     * @param rowKey 行键
     * @return 行数据
     */
    Map<String, Object> getRow(String tableName, String rowKey);
    
    /**
     * 扫描表
     * @param tableName 表名
     * @param startRow 起始行键
     * @param stopRow 结束行键
     * @param limit 限制返回的行数
     * @return 行数据列表
     */
    List<Map<String, Object>> scanTable(String tableName, String startRow, String stopRow, int limit);
    
    /**
     * 删除一行数据
     * @param tableName 表名
     * @param rowKey 行键
     * @return 是否删除成功
     */
    boolean deleteRow(String tableName, String rowKey);
    
    /**
     * 批量删除数据
     * @param tableName 表名
     * @param rowKeys 行键列表
     * @return 是否删除成功
     */
    boolean deleteRows(String tableName, List<String> rowKeys);
    
    /**
     * 计算表中的行数
     * @param tableName 表名
     * @return 行数
     */
    long countRows(String tableName);
}