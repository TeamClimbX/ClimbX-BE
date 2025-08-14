package com.climbx.climbx.problem.enums;

import com.climbx.climbx.common.exception.InvalidRatingValueException;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ProblemTierType {

    M(30, 30, 31),
    D1(28, 29, 30),
    D2(26, 27, 28),
    D3(24, 25, 26),
    P1(22, 23, 24),
    P2(20, 21, 22),
    P3(18, 19, 20),
    G1(16, 17, 18),
    G2(14, 15, 16),
    G3(12, 13, 14),
    S1(10, 11, 12),
    S2(8, 9, 10),
    S3(6, 7, 8),
    B1(4, 5, 6),
    B2(2, 3, 4),
    B3(0, 1, 2);

    private final int inclusiveMinValue;
    private final int value;
    private final int exclusiveMaxValue;

    public static ProblemTierType fromValue(Integer value) {
        return Stream.of(values())
            .filter(t -> t.inclusiveMinValue <= value && value < t.exclusiveMaxValue)
            .findFirst()
            .orElseThrow(() -> new InvalidRatingValueException(value));
    }
}