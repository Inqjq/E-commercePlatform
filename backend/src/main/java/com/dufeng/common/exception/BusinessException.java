package com.dufeng.common.exception;

import com.dufeng.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常，统一由全局异常处理器转换为 {@code Result} 响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
