package com.climbx.climbx.common.enums;

import com.climbx.climbx.common.exception.InvalidEnumValueException;
import com.climbx.climbx.common.util.OptionalUtil;

public enum ActiveStatusType {

    ACTIVE,
    INACTIVE;

    public static ActiveStatusType from(String value) {
        return OptionalUtil.tryOf(() -> valueOf(value))
            .orElseThrow(() -> new InvalidEnumValueException("ActiveStatusType", value));
    }
}
