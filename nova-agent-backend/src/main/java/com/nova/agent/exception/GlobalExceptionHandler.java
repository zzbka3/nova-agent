package com.nova.agent.exception;

import com.nova.agent.model.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AgentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleAgentException(AgentException e) {
        log.error("AgentException: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(AgentFlowConstructException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleAgentFlowConstructException(AgentFlowConstructException e) {
        log.error("AgentFlowConstructException: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthException(AuthException e) {
        log.error("AuthException: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.error(msg);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return Result.error("系统内部错误");
    }
}
