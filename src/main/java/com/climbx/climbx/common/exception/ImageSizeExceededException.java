package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;

public class ImageSizeExceededException extends BusinessException {

    public ImageSizeExceededException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ImageSizeExceededException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
