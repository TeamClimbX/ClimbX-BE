package com.climbx.climbx.common.enums;

import com.climbx.climbx.common.exception.InvalidEnumValueException;
import com.climbx.climbx.common.util.OptionalUtil;

public enum TokenType {

    ACCESS,
    REFRESH;

    public static TokenType from(String value) {
        return OptionalUtil.tryOf(() -> valueOf(value))
            .orElseThrow(() -> new InvalidEnumValueException("TokenType", value));
    }
}
