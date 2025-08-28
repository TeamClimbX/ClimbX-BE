package com.climbx.climbx.video.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class VideoOnlyOwnerCanModifyException extends BusinessException {

    public VideoOnlyOwnerCanModifyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VideoOnlyOwnerCanModifyException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
