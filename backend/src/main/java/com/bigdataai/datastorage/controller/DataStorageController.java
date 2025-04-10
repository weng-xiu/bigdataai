package com.bigdataai.datastorage.controller;

import com.bigdataai.datastorage.service.DataStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据存储控制器
 */
@RestController
@RequestMapping("/storage")
public class DataStorageController {

    @Autowired
    private DataStorageService dataStorageService;

    /**
     * HDFS文件上传
     */
    @PostMapping("/hdfs/upload")
    public ResponseEntity<Map<String, Object>> uploadToHDFS(
            @RequestParam("file") MultipartFile file,
            @RequestParam("path") String hdfsPath,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = dataStorageService.storeToHDFS(hdfsPath, file.getInputStream(), overwrite);
            
            if (success) {
                result.put("success", true);
                result.put("message", "文件上传成功");
                result.put("path", hdfsPath);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "文件上传失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * HDFS目录列表
     */
    @GetMapping("/hdfs/list")
    public ResponseEntity<List<Map<String, Object>>> listHDFSDirectory(@RequestParam("path") String dirPath) {
        try {
            List<Map<String, Object>> fileList = dataStorageService.listHDFSDirectory(dirPath);
            return ResponseEntity.ok(fileList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * HDFS文件删除
     */
    @DeleteMapping("/hdfs")
    public ResponseEntity<Map<String, Object>> deleteHDFSPath(
            @RequestParam("path") String path,
            @RequestParam(value = "recursive", defaultValue = "false") boolean recursive) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = dataStorageService.deleteHDFSPath(path, recursive);
            
            if (success) {
                result.put("success", true);
                result.put("message", "文件/目录删除成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "文件/目录删除失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件/目录删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * HBase数据存储
     */
    @PostMapping("/hbase")
    public ResponseEntity<Map<String, Object>> storeToHBase(
            @RequestParam("tableName") String tableName,
            @RequestParam("rowKey") String rowKey,
            @RequestParam("familyName") String familyName,
            @RequestBody Map<String, Object> data) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> qualifiers = (List<String>) data.get("qualifiers");
            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) data.get("values");
            
            boolean success = dataStorageService.storeToHBase(tableName, rowKey, familyName, qualifiers, values);
            
            if (success) {
                result.put("success", true);
                result.put("message", "数据存储成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "数据存储失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据存储失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * HBase批量数据存储
     */
    @PostMapping("/hbase/batch")
    public ResponseEntity<Map<String, Object>> batchStoreToHBase(
            @RequestParam("tableName") String tableName,
            @RequestBody List<Map<String, Object>> data) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = dataStorageService.batchStoreToHBase(tableName, data);
            
            if (success) {
                result.put("success", true);
                result.put("message", "批量数据存储成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "批量数据存储失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量数据存储失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * HBase数据读取
     */
    @GetMapping("/hbase")
    public ResponseEntity<Map<String, String>> readFromHBase(
            @RequestParam("tableName") String tableName,
            @RequestParam("rowKey") String rowKey,
            @RequestParam(value = "familyName", required = false) String familyName) {
        
        try {
            Map<String, String> data = dataStorageService.readFromHBase(tableName, rowKey, familyName);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * HBase表扫描
     */
    @GetMapping("/hbase/scan")
    public ResponseEntity<List<Map<String, String>>> scanHBase(
            @RequestParam("tableName") String tableName,
            @RequestParam(value = "startRow", required = false) String startRow,
            @RequestParam(value = "endRow", required = false) String endRow,
            @RequestParam(value = "familyName", required = false) String familyName) {
        
        try {
            List<Map<String, String>> data = dataStorageService.scanHBase(tableName, startRow, endRow, familyName);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * HBase数据删除
     */
    @DeleteMapping("/hbase")
    public ResponseEntity<Map<String, Object>> deleteFromHBase(
            @RequestParam("tableName") String tableName,
            @RequestParam("rowKey") String rowKey,
            @RequestParam(value = "familyName", required = false) String familyName) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = dataStorageService.deleteFromHBase(tableName, rowKey, familyName);
            
            if (success) {
                result.put("success", true);
                result.put("message", "数据删除成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "数据删除失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Elasticsearch索引文档
     */
    @PostMapping("/elasticsearch/index")
    public ResponseEntity<?> indexElasticsearchDocument(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> result = new HashMap<>();
        try {
            String indexName = (String) requestBody.get("indexName");
            Map<String, Object> document = (Map<String, Object>) requestBody.get("document");
            String id = (String) requestBody.get("id");
            
            boolean success = dataStorageService.indexToElasticsearch(indexName, id, document);
            
            if (success) {
                result.put("success", true);
                result.put("message", "文档索引成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "文档索引失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文档索引失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}