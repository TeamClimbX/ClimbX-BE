package com.climbx.climbx.common.scheduler.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class RankingSnapshotCreationException extends BusinessException {

    public RankingSnapshotCreationException(Long userId, Throwable cause) {
        super(ErrorCode.RANKING_SNAPSHOT_CREATION_FAILED);
        addContext("userId", String.valueOf(userId));
        addContext("originalError", cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        initCause(cause);
    }
}