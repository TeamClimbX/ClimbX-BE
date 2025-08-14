package com.climbx.climbx.user.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class CannotCreateUserNickname extends BusinessException {

    public CannotCreateUserNickname(ErrorCode errorCode) {
        super(errorCode);
    }

    public CannotCreateUserNickname(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
