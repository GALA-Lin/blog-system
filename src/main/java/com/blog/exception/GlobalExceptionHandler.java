package com.blog.exception;

import com.blog.common.BusinessException;
import com.blog.common.Result;
import com.blog.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-15:17
 * @Description: 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler  {
    /**
     * 业务异常处理器
     * @param e
     * @return Result
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数验证异常处理器
     * @param e
     * @return Result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("验证失败: {}", errors);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "参数验证失败" + errors);
    }
    /**
     * 参数绑定异常处理器
     * @param e
     * @return Result
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        FieldError fieldError = e.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation Failed";
        log.error("Bind Exception: {}", message);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 权限异常处理器
     * @param e
     * @return
     */
    @ExceptionHandler({AccessDeniedException.class, java.nio.file.AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(Exception e) {
        log.error("Access Denied: {}", e.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }

    /**
     * 认证异常处理器
     * @param e
     * @return
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Bad Credentials: {}", e.getMessage());
        return Result.error(ResultCode.INVALID_CREDENTIALS);
    }

    /**
     * 参数异常处理器
     * @param e
     * @return
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal Argument: {}", e.getMessage());
        return Result.error(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 未知异常处理器
     * @param e
     * @return Result
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("System Exception: {}", e.getMessage());
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常，请联系管理员");
    }
}
