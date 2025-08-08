package com.climbx.climbx.problem.dto;

import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record ContributionRequestDto(

    @NotNull
    ProblemTierType tier,

    String comment, // nullable, 예시: "문제 어디가 어려웠고, 추천할 만한 문제인지 등"

    List<ProblemTagType> tags // nullable
) {

}
