package com.blog.common;

import lombok.Getter;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-06-15:13
 * @Description:
 */
@Getter
public class BusinessException extends RuntimeException  {
    private final Integer code;
    private final String message;
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
    }
}
