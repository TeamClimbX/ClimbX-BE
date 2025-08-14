package com.climbx.climbx.submission.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import java.util.UUID;

public class ForbiddenSubmissionException extends BusinessException {

    public ForbiddenSubmissionException(Long userId, UUID videoId) {
        super(ErrorCode.FORBIDDEN_SUBMISSION);
        addContext("userId", userId.toString());
        addContext("videoId", videoId.toString());
    }
}
