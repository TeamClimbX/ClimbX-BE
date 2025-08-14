package com.climbx.climbx.problem.dto;

import com.climbx.climbx.gym.enums.GymTierType;
import com.climbx.climbx.problem.enums.HoldColorType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProblemCreateRequestDto(

    @NotNull
    Long gymAreaId,

    @NotNull
    GymTierType localLevel,

    @NotNull
    HoldColorType holdColor
) {

}