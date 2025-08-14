package com.climbx.climbx.submission.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import java.util.UUID;

public class DuplicateAppealException extends BusinessException {

    public DuplicateAppealException(UUID videoId) {
        super(ErrorCode.DUPLICATE_APPEAL, "이미 이의신청이 접수된 제출물입니다: " + videoId);
    }
} 