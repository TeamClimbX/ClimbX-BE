package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.enums.OutboxEventType;

public class OutboxEventRecordException extends BusinessException {

    public OutboxEventRecordException(String aggregateType, String aggregateId, 
                                     OutboxEventType eventType, Throwable cause) {
        super(ErrorCode.OUTBOX_EVENT_RECORD_FAILED);
        addContext("aggregateType", aggregateType);
        addContext("aggregateId", aggregateId);
        addContext("eventType", String.valueOf(eventType));
        addContext("originalError", cause.getMessage() != null
                   ? cause.getMessage() : cause.getClass().getSimpleName());
        initCause(cause);
    }
}