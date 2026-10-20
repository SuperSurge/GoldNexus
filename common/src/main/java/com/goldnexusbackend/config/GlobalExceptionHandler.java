package com.goldnexusbackend.config;

import com.goldnexusbackend.entity.Res;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：未捕获异常统一返回 Res(500)，避免被 Spring Security 误转为 401。
 * 供所有依赖 common 的业务服务(user/loan/content/points)使用。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Res handleRuntime(RuntimeException e) {
        log.error("未捕获业务异常: {}", e.getMessage(), e);
        return new Res(500, e.getMessage() == null ? "系统异常" : e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public Res handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return new Res(500, "系统异常", null);
    }
}
