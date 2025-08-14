package com.climbx.climbx.problem.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import java.util.UUID;

public class ForbiddenProblemVoteException extends BusinessException {

    public ForbiddenProblemVoteException(UUID problemId, Long userId) {
        super(ErrorCode.FORBIDDEN_PROBLEM_VOTE);
        addContext("problemId", problemId.toString());
        addContext("userId", userId.toString());
    }
}
