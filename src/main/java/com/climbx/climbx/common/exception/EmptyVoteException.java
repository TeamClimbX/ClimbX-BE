package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;

public class EmptyVoteException extends BusinessException {

    public EmptyVoteException() {
        super(ErrorCode.EMPTY_VOTE);
    }
}
