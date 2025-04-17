package com.bigdataai.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 注解校验失败的异常
     * @param ex MethodArgumentNotValidException
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("message", "请求参数验证失败");
        responseBody.put("errors", errors);
        return ResponseEntity.badRequest().body(responseBody);
    }

    /**
     * 处理其他未捕获的异常
     * @param ex Exception
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtException(Exception ex) {
        // 在实际应用中，这里应该记录详细的错误日志
        ex.printStackTrace(); // 仅用于开发阶段

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("message", "服务器内部错误: " + ex.getMessage());
        // 避免在生产环境中暴露过多细节
        // responseBody.put("details", ex.toString()); 
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }

    // 可以根据需要添加更多特定异常的处理方法
    // 例如：处理自定义业务异常
    /*
    @ExceptionHandler(CustomBusinessException.class)
    public ResponseEntity<Map<String, Object>> handleCustomBusinessException(CustomBusinessException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("message", ex.getMessage());
        responseBody.put("code", ex.getErrorCode()); // 如果有错误码
        return ResponseEntity.status(ex.getHttpStatus()).body(responseBody);
    }
    */
}