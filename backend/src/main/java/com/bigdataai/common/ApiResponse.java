package com.bigdataai.common;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {

    private String status;
    private String message;
    private Object data;

    private ApiResponse(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Map<String, Object> success(String message) {
        return success(message, null);
    }

    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    public static Map<String, Object> error(String message) {
        return error(message, null);
    }

    public static Map<String, Object> error(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    // Getters (optional, depending on how you use it)
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}