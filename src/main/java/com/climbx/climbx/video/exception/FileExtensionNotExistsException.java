package com.climbx.climbx.video.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class FileExtensionNotExistsException extends BusinessException {

    public FileExtensionNotExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FileExtensionNotExistsException(
        ErrorCode errorCode,
        String detail
    ) {
        super(errorCode, detail);
    }
}
