package com.climbx.climbx.common.comcode.exception;

import com.climbx.climbx.common.error.BusinessException;
import com.climbx.climbx.common.error.ErrorCode;

public class ComcodeNotFound extends BusinessException {

    public ComcodeNotFound(String code) {
        super(ErrorCode.COMCODE_NOT_FOUND);
        addContext("wrong code ", code);
    }
}
