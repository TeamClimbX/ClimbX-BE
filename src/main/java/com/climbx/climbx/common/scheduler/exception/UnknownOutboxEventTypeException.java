package com.climbx.climbx.common.scheduler.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class UnknownOutboxEventTypeException extends BusinessException {

    public UnknownOutboxEventTypeException(String eventType, String aggregateId) {
        super(ErrorCode.UNKNOWN_OUTBOX_EVENT_TYPE);
        addContext("eventType", eventType);
        addContext("aggregateId", aggregateId);
    }
}