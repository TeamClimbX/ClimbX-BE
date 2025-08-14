package com.climbx.climbx.problem.dto;

import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.gym.enums.GymTierType;
import com.climbx.climbx.problem.enums.HoldColorType;
import com.climbx.climbx.problem.entity.ProblemEntity;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProblemCreateResponseDto(

    UUID problemId,
    Long gymId,
    String gymName,
    Long gymAreaId,
    String areaName,
    GymTierType localLevel,
    HoldColorType holdColor,
    Integer problemRating,
    String problemImageCdnUrl,
    ActiveStatusType activeStatus
) {

    public static ProblemCreateResponseDto from(ProblemEntity problem) {
        return ProblemCreateResponseDto.builder()
            .problemId(problem.problemId())
            .gymId(problem.gymEntity().gymId())
            .gymName(problem.gymEntity().name())
            .gymAreaId(problem.gymArea().gymAreaId())
            .areaName(problem.gymArea().areaName())
            .localLevel(problem.localLevel())
            .holdColor(problem.holdColor())
            .problemRating(problem.rating())
            .problemImageCdnUrl(problem.problemImageCdnUrl())
            .activeStatus(problem.activeStatus())
            .build();
    }
}