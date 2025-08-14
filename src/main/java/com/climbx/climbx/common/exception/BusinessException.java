package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, String> context = new HashMap<>();

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.message());
        this.errorCode = errorCode;
        context.put("detail", detail);
    }

    protected void addContext(String key, String value) {
        context.put(key, value);
    }
}
