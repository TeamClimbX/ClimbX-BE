package com.climbx.climbx.user.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class NicknameMismatchException extends BusinessException {

    public NicknameMismatchException(String pathNickname, String bodyNickname) {
        super(ErrorCode.NICKNAME_MISMATCH);
        addContext("pathNickname", pathNickname);
        addContext("bodyNickname", bodyNickname);
    }
}