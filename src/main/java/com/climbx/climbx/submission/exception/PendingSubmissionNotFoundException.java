package com.climbx.climbx.submission.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import java.util.UUID;

public class PendingSubmissionNotFoundException extends BusinessException {

    public PendingSubmissionNotFoundException(UUID videoId) {
        super(ErrorCode.PENDING_SUBMISSION_NOT_FOUND);
        addContext("videoId", videoId.toString());
    }
}
