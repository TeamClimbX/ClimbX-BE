package com.climbx.climbx.common.scheduler.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class UserRatingUpdateException extends BusinessException {

    public UserRatingUpdateException(Long userId, Throwable cause) {
        super(ErrorCode.USER_RATING_UPDATE_FAILED);
        addContext("userId", String.valueOf(userId));
        addContext("originalError", cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        initCause(cause);
    }
}