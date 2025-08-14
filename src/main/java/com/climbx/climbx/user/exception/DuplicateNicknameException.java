package com.climbx.climbx.user.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class DuplicateNicknameException extends BusinessException {

    public DuplicateNicknameException(String nickname) {
        super(ErrorCode.DUPLICATED_NICKNAME);
        addContext("nickname", nickname);
    }
}