package com.climbx.climbx.video.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class AwsCloudFrontDomainNotConfiguredException extends BusinessException {

    public AwsCloudFrontDomainNotConfiguredException(
        ErrorCode errorCode) {
        super(errorCode);
    }

    public AwsCloudFrontDomainNotConfiguredException(ErrorCode errorCode,
        String detail) {
        super(errorCode, detail);
    }
}
