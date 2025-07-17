package com.climbx.climbx.auth.exception;

import com.climbx.climbx.common.error.BusinessException;
import com.climbx.climbx.common.error.ErrorCode;

public class UserAuthNotFoundException extends BusinessException {

    public UserAuthNotFoundException(Long userId) {
        super(ErrorCode.USER_AUTH_NOT_FOUND);
        addContext("userId", userId.toString());
    }
}
