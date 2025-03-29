package com.bigdataai.datastorage.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HBase存储服务实现类
 */
@Service
public class HBaseServiceImpl implements HBaseService {

    @Value("${hbase.zookeeper.quorum}")
    private String zookeeperQuorum;

    @Value("${hbase.zookeeper.port}")
    private String zookeeperPort;

    /**
     * 获取HBase配置
     */
    private Configuration getConfiguration() {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", zookeeperQuorum);
        configuration.set("hbase.zookeeper.property.clientPort", zookeeperPort);
        return configuration;
    }

    /**
     * 获取HBase连接
     */
    private Connection getConnection() throws IOException {
        return ConnectionFactory.createConnection(getConfiguration());
    }

    @Override
    public boolean createTable(String tableName, String[] columnFamilies) {
        try (Connection connection = getConnection();
             Admin admin = connection.getAdmin()) {

            TableName name = TableName.valueOf(tableName);

            // 检查表是否已存在
            if (admin.tableExists(name)) {
                return false;
            }

            // 创建表描述符
            HTableDescriptor tableDescriptor = new HTableDescriptor(name);

            // 添加列族
            for (String columnFamily : columnFamilies) {
                tableDescriptor.addFamily(new HColumnDescriptor(columnFamily));
            }

            // 创建表
            admin.createTable(tableDescriptor);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("创建HBase表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteTable(String tableName) {
        try (Connection connection = getConnection();
             Admin admin = connection.getAdmin()) {

            TableName name = TableName.valueOf(tableName);

            // 检查表是否存在
            if (!admin.tableExists(name)) {
                return false;
            }

            // 禁用表
            if (!admin.isTableDisabled(name)) {
                admin.disableTable(name);
            }

            // 删除表
            admin.deleteTable(name);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("删除HBase表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean tableExists(String tableName) {
        try (Connection connection = getConnection();
             Admin admin = connection.getAdmin()) {

            return admin.tableExists(TableName.valueOf(tableName));
        } catch (IOException e) {
            throw new RuntimeException("检查HBase表是否存在失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> listTables() {
        try (Connection connection = getConnection();
             Admin admin = connection.getAdmin()) {

            HTableDescriptor[] tableDescriptors = admin.listTables();
            List<String> tableNames = new ArrayList<>();

            for (HTableDescriptor tableDescriptor : tableDescriptors) {
                tableNames.add(tableDescriptor.getNameAsString());
            }

            return tableNames;
        } catch (IOException e) {
            throw new RuntimeException("列出HBase表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getTableDescription(String tableName) {
        try (Connection connection = getConnection();
             Admin admin = connection.getAdmin()) {

            TableName name = TableName.valueOf(tableName);
            
            // 检查表是否存在
            if (!admin.tableExists(name)) {
                return null;
            }
            
            HTableDescriptor tableDescriptor = admin.getTableDescriptor(name);
            Map<String, Object> description = new HashMap<>();
            
            description.put("tableName", tableDescriptor.getNameAsString());
            
            // 获取列族信息
            List<Map<String, Object>> columnFamilies = new ArrayList<>();
            for (HColumnDescriptor columnDescriptor : tableDescriptor.getColumnFamilies()) {
                Map<String, Object> columnFamily = new HashMap<>();
                columnFamily.put("name", columnDescriptor.getNameAsString());
                columnFamily.put("maxVersions", columnDescriptor.getMaxVersions());
                columnFamily.put("timeToLive", columnDescriptor.getTimeToLive());
                columnFamily.put("blockSize", columnDescriptor.getBlocksize());
                columnFamily.put("compression", columnDescriptor.getCompressionType().toString());
                columnFamilies.add(columnFamily);
            }
            description.put("columnFamilies", columnFamilies);
            
            return description;
        } catch (IOException e) {
            throw new RuntimeException("获取HBase表描述失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean addColumnFamily(String tableName, String columnFamily) {
        try (Connection connection = getConnection();
             Admin admin = connection.getAdmin()) {

            TableName name = TableName.valueOf(tableName);
            
            // 检查表是否存在
            if (!admin.tableExists(name)) {
                return false;
            }
            
            // 禁用表
            if (!admin.isTableDisabled(name)) {
                admin.disableTable(name);
            }
            
            // 添加列族
            admin.addColumn(name, new HColumnDescriptor(columnFamily));
            
            // 启用表
            admin.enableTable(name);
            
            return true;
        } catch (IOException e) {
            throw new RuntimeException("添加HBase列族失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteColumnFamily(String tableName, String columnFamily) {
        try (Connection connection = getConnection();
             Admin admin = connection.getAdmin()) {

            TableName name = TableName.valueOf(tableName);
            
            // 检查表是否存在
            if (!admin.tableExists(name)) {
                return false;
            }
            
            // 禁用表
            if (!admin.isTableDisabled(name)) {
                admin.disableTable(name);
            }
            
            // 删除列族
            admin.deleteColumn(name, Bytes.toBytes(columnFamily));
            
            // 启用表
            admin.enableTable(name);
            
            return true;
        } catch (IOException e) {
            throw new RuntimeException("删除HBase列族失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean putRow(String tableName, String rowKey, String columnFamily, String column, String value) {
        try (Connection connection = getConnection();
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
            table.put(put);
            
            return true;
        } catch (IOException e) {
            throw new RuntimeException("HBase插入数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean putRows(String tableName, List<Map<String, Object>> rows) {
        try (Connection connection = getConnection();
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            List<Put> puts = new ArrayList<>();
            
            for (Map<String, Object> row : rows) {
                String rowKey = (String) row.get("rowKey");
                Map<String, Map<String, String>> families = (Map<String, Map<String, String>>) row.get("columnFamilies");
                
                Put put = new Put(Bytes.toBytes(rowKey));
                
                for (Map.Entry<String, Map<String, String>> familyEntry : families.entrySet()) {
                    String familyName = familyEntry.getKey();
                    Map<String, String> columns = familyEntry.getValue();
                    
                    for (Map.Entry<String, String> columnEntry : columns.entrySet()) {
                        String columnName = columnEntry.getKey();
                        String value = columnEntry.getValue();
                        
                        put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName), Bytes.toBytes(value));
                    }
                }
                
                puts.add(put);
            }
            
            table.put(puts);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("HBase批量插入数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getRow(String tableName, String rowKey) {
        try (Connection connection = getConnection();
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            Get get = new Get(Bytes.toBytes(rowKey));
            Result result = table.get(get);
            
            if (result.isEmpty()) {
                return null;
            }
            
            return resultToMap(rowKey, result);
        } catch (IOException e) {
            throw new RuntimeException("HBase获取数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> scanTable(String tableName, String startRow, String stopRow, int limit) {
        try (Connection connection = getConnection();
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            Scan scan = new Scan();
            
            if (startRow != null && !startRow.isEmpty()) {
                scan.setStartRow(Bytes.toBytes(startRow));
            }
            
            if (stopRow != null && !stopRow.isEmpty()) {
                scan.setStopRow(Bytes.toBytes(stopRow));
            }
            
            if (limit > 0) {
                scan.setMaxResultSize(limit);
            }
            
            ResultScanner scanner = table.getScanner(scan);
            List<Map<String, Object>> results = new ArrayList<>();
            
            for (Result result : scanner) {
                String row = Bytes.toString(result.getRow());
                Map<String, Object> rowMap = resultToMap(row, result);
                results.add(rowMap);
                
                if (limit > 0 && results.size() >= limit) {
                    break;
                }
            }
            
            scanner.close();
            return results;
        } catch (IOException e) {
            throw new RuntimeException("HBase扫描表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteRow(String tableName, String rowKey) {
        try (Connection connection = getConnection();
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
            
            return true;
        } catch (IOException e) {
            throw new RuntimeException("HBase删除数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteRows(String tableName, List<String> rowKeys) {
        try (Connection connection = getConnection();
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            List<Delete> deletes = new ArrayList<>();
            
            for (String rowKey : rowKeys) {
                Delete delete = new Delete(Bytes.toBytes(rowKey));
                deletes.add(delete);
            }
            
            table.delete(deletes);
            
            return true;
        } catch (IOException e) {
            throw new RuntimeException("HBase批量删除数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long countRows(String tableName) {
        try (Connection connection = getConnection();
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            Scan scan = new Scan();
            scan.setFilter(new FirstKeyOnlyFilter());
            
            ResultScanner scanner = table.getScanner(scan);
            long count = 0;
            
            for (Result result : scanner) {
                count++;
            }
            
            scanner.close();
            return count;
        } catch (IOException e) {
            throw new RuntimeException("HBase计数失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将Result对象转换为Map
     */
    private Map<String, Object> resultToMap(String rowKey, Result result) {
        Map<String, Object> rowMap = new HashMap<>();
        rowMap.put("rowKey", rowKey);
        
        Map<String, Map<String, String>> familyMap = new HashMap<>();
        
        for (Cell cell : result.rawCells()) {
            String family = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
            String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            
            Map<String, String> qualifierMap = familyMap.computeIfAbsent(family, k -> new HashMap<>());
            qualifierMap.put(qualifier, value);
        }
        
        rowMap.put("columnFamilies", familyMap);
        return rowMap;
    }
}