package com.bigdataai.monitoring.controller;

import com.bigdataai.monitoring.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息控制器
 */
@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    @Autowired
    private MonitoringService monitoringService;

    /**
     * 获取系统基本信息
     * @return 系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        try {
            // 调用服务层获取信息
            Map<String, Object> systemInfo = monitoringService.getSystemBasicInfo(); 
            return ResponseEntity.ok(systemInfo);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取系统信息失败: " + e.getMessage());
            // Consider logging the exception e
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // 可以根据需要添加更多获取详细监控信息的端点
    // 例如：获取节点状态、存储使用情况等

}