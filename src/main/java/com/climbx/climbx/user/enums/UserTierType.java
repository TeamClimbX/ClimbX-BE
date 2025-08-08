package com.climbx.climbx.user.enums;

import com.climbx.climbx.common.exception.InvalidRatingValueException;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum UserTierType {

    M(2250, 3101),
    D1(2100, 2250),
    D2(1950, 2100),
    D3(1800, 1950),
    P1(1650, 1800),
    P2(1500, 1650),
    P3(1350, 1500),
    G1(1200, 1350),
    G2(1050, 1200),
    G3(900, 1050),
    S1(750, 900),
    S2(600, 750),
    S3(450, 600),
    B1(300, 450),
    B2(150, 300),
    B3(0, 150);

    private final int inclusiveMinValue;
    private final int exclusiveMaxValue;

    public static UserTierType fromValue(Integer value) {
        return Stream.of(values())
            .filter(t -> t.inclusiveMinValue <= value && value < t.exclusiveMaxValue)
            .findFirst()
            .orElseThrow(() -> new InvalidRatingValueException(value));
    }
}
