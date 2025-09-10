package com.climbx.climbx.common.scheduler.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class OutboxEventProcessingException extends BusinessException {

    public OutboxEventProcessingException(String eventType, String aggregateId, Throwable cause) {
        super(ErrorCode.OUTBOX_EVENT_PROCESSING_FAILED);
        addContext("eventType", eventType);
        addContext("aggregateId", aggregateId);
        addContext("originalError", cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        initCause(cause);
    }
}