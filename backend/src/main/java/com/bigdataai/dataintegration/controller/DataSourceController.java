package com.bigdataai.dataintegration.controller;

import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.model.DataSourceType;
import com.bigdataai.dataintegration.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据源控制器
 */
@RestController
@RequestMapping("/datasources")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 创建数据源
     */
    @PostMapping
    public ResponseEntity<DataSource> createDataSource(@RequestBody DataSource dataSource) {
        DataSource createdDataSource = dataSourceService.createDataSource(dataSource);
        return new ResponseEntity<>(createdDataSource, HttpStatus.CREATED);
    }

    /**
     * 更新数据源
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataSource> updateDataSource(@PathVariable Long id, @RequestBody DataSource dataSource) {
        dataSource.setId(id);
        DataSource updatedDataSource = dataSourceService.updateDataSource(dataSource);
        return ResponseEntity.ok(updatedDataSource);
    }

    /**
     * 获取数据源
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataSource> getDataSource(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getDataSource(id);
        if (dataSource != null) {
            return ResponseEntity.ok(dataSource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取所有数据源
     */
    @GetMapping
    public ResponseEntity<List<DataSource>> getAllDataSources() {
        List<DataSource> dataSources = dataSourceService.getAllDataSources();
        return ResponseEntity.ok(dataSources);
    }

    /**
     * 根据类型获取数据源
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<DataSource>> getDataSourcesByType(@PathVariable DataSourceType type) {
        List<DataSource> dataSources = dataSourceService.getDataSourcesByType(type);
        return ResponseEntity.ok(dataSources);
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable Long id) {
        boolean deleted = dataSourceService.deleteDataSource(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/{id}/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getDataSource(id);
        if (dataSource == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> result = dataSourceService.testConnection(dataSource);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取数据源表/集合列表
     */
    @GetMapping("/{id}/tables")
    public ResponseEntity<List<String>> getTableList(@PathVariable Long id) {
        try {
            List<String> tables = dataSourceService.getTableList(id);
            return ResponseEntity.ok(tables);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取表/集合结构
     */
    @GetMapping("/{id}/tables/{tableName}/schema")
    public ResponseEntity<List<Map<String, Object>>> getTableSchema(
            @PathVariable Long id,
            @PathVariable String tableName) {
        try {
            List<Map<String, Object>> schema = dataSourceService.getTableSchema(id, tableName);
            return ResponseEntity.ok(schema);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}