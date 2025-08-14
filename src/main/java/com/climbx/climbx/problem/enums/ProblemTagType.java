package com.climbx.climbx.problem.enums;

import com.climbx.climbx.common.exception.InvalidEnumValueException;
import com.climbx.climbx.common.util.OptionalUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ProblemTagType {

    PINCH_HOLD("핀치 홀드", 3),
    CRIMP_HOLD("크림프 홀드", 7),
    SLOPER_HOLD("슬로퍼 홀드", 8),
    POCKET_HOLD("포켓 홀드", 9),

    TOE_HOOK("토 훅", 10),
    HEEL_HOOK("힐 훅", 6),
    DROP_KNEE("드롭 니", 7),

    REACH("리치", 11),
    BALANCE("밸런스", 14),
    OVERHANG("오버행", 4),
    BATHANG("배트행", 5),

    LUNGE("런지", 2),
    DYNO("다이노", 13),
    COORDINATE("코디네이션", 1);

    private final String displayName;
    private final int priority;

    public static ProblemTagType from(String name) {
        return OptionalUtil.tryOf(() -> valueOf(name))
            .orElseThrow(() -> new InvalidEnumValueException("ProblemType", name));
    }
}
