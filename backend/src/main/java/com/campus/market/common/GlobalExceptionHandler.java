package com.campus.market.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> business(BusinessException e) {
        int family = e.getCode() / 100;
        int status = family == 401 ? 401 : family == 403 ? 403 : family == 404 ? 404 : family >= 500 ? 500 : 400;
        return ResponseEntity.status(status).body(ApiResponse.error(e.getCode(), e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(x -> x.getDefaultMessage()).orElse("参数错误");
        return ResponseEntity.badRequest().body(ApiResponse.error(40001, message));
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> denied() {
        return ResponseEntity.status(403).body(ApiResponse.error(40301, "无权限执行此操作"));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> malformed() {
        return ResponseEntity.badRequest().body(ApiResponse.error(40001, "请求参数格式错误"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> conflict() {
        return ResponseEntity.badRequest().body(ApiResponse.error(40001, "数据已存在或关联状态不允许此操作"));
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> uploadTooLarge() {
        return ResponseEntity.badRequest().body(ApiResponse.error(40001, "图片大小不能超过 5MB"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unknown(Exception e) {
        log.error("Unhandled request error", e);
        return ResponseEntity.internalServerError().body(ApiResponse.error(50000, "系统异常"));
    }
}
