package com.climbx.climbx.admin.submission.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import java.util.UUID;

public class StatusModifyToPendingException extends BusinessException {

    public StatusModifyToPendingException(UUID videoId) {
        super(ErrorCode.STATUS_MODIFY_TO_PENDING);
        addContext("videoId", videoId.toString());
    }
}
