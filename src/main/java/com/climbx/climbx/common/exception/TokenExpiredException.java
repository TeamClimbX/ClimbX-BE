package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;

public class TokenExpiredException extends BusinessException {

    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }

    public TokenExpiredException(String tokenType) {
        super(ErrorCode.TOKEN_EXPIRED);
        addContext("tokenType", tokenType);
    }
} 