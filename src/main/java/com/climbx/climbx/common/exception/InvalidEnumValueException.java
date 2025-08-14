package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;

public class InvalidEnumValueException extends BusinessException {

    public InvalidEnumValueException(String type, String name) {
        super(ErrorCode.INVALID_ENUM_VALUE);
        addContext("type", type);
        addContext("name", name);
    }
}
