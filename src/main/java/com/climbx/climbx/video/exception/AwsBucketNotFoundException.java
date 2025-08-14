package com.climbx.climbx.video.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class AwsBucketNotFoundException extends BusinessException {

    public AwsBucketNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AwsBucketNotFoundException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
