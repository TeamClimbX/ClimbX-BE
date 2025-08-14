package com.climbx.climbx.submission.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import java.util.UUID;

public class DuplicateSubmissionException extends BusinessException {

    public DuplicateSubmissionException(UUID videoId) {
        super(ErrorCode.DUPLICATE_SUBMISSION, "이미 제출된 영상입니다: " + videoId);
    }
} 