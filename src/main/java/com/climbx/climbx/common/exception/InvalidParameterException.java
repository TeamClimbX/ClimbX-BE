package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;

public class InvalidParameterException extends BusinessException {

    public InvalidParameterException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidParameterException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
