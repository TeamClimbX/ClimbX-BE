package com.climbx.climbx.common.enums;

import com.climbx.climbx.common.exception.InvalidEnumValueException;
import com.climbx.climbx.common.util.OptionalUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CriteriaType {

    RATING("rating"),
    CURRENT_STREAK("currentStreak"),
    LONGEST_STREAK("longestStreak"),
    SOLVED_COUNT("solvedCount");

    private final String fieldName;

    public static CriteriaType from(String code) {
        return OptionalUtil.tryOf(() -> valueOf(code))
            .orElseThrow(() -> new InvalidEnumValueException("CriteriaType", code));
    }
}
