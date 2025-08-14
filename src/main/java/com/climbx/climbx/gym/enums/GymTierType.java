package com.climbx.climbx.gym.enums;

import com.climbx.climbx.problem.enums.ProblemTierType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GymTierType {

    WHITE("흰색", ProblemTierType.B3),
    YELLOW("노랑", ProblemTierType.B2),
    ORANGE("주황", ProblemTierType.B1),
    GREEN("초록", ProblemTierType.S2),
    BLUE("파랑", ProblemTierType.G2),
    RED("빨강", ProblemTierType.P3),
    PURPLE("보라", ProblemTierType.P1),
    GRAY("회색", ProblemTierType.D3),
    BROWN("갈색", ProblemTierType.D1),
    BLACK("검정", ProblemTierType.M);

    private final String displayName;
    private final ProblemTierType globalTier;
}
