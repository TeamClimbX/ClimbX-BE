package com.climbx.climbx.problem.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class ProblemAlreadyDeletedException extends BusinessException {

    public ProblemAlreadyDeletedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProblemAlreadyDeletedException(ErrorCode errorCode,
        String detail) {
        super(errorCode, detail);
    }
}
