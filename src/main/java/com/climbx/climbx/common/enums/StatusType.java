package com.climbx.climbx.common.enums;

import com.climbx.climbx.common.exception.InvalidEnumValueException;
import com.climbx.climbx.common.util.OptionalUtil;

public enum StatusType {

    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED;

    public static StatusType from(String value) {
        return OptionalUtil.tryOf(
            () -> StatusType.valueOf(value.toUpperCase())
        ).orElseThrow(
            () -> new InvalidEnumValueException("StatusType", value)
        );
    }
}
