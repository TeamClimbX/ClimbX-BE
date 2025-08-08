package com.climbx.climbx.problem.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum HoldColorType {

    /**
     * TODO: ProblemEntity에 enum으로 변경, Gym <-> HoldColorType 매핑된 기본 티어 테이블 구성
     */

    WHITE("흰색"),
    YELLOW("노랑"),
    ORANGE("주황"),
    GREEN("초록"),
    BLUE("파랑"),
    RED("빨강"),
    PURPLE("보라"),
    GRAY("회색"),
    BROWN("갈색"),
    BLACK("검정");

    private final String displayName;
}
