package com.climbx.climbx.video.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class AwsBucketNameNotConfiguredException extends BusinessException {

    public AwsBucketNameNotConfiguredException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AwsBucketNameNotConfiguredException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
