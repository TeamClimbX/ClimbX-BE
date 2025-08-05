package com.climbx.climbx.problem.dto;

import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.problem.entity.GymAreaEntity;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.enums.ProblemTierType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProblemInfoResponseDto(

    UUID problemId,
    Long gymId,
    String gymName,
    Long gymAreaId,
    String gymAreaName,
    String localLevel,
    String holdColor,
    Integer problemRating,
    ProblemTierType problemTier,
    String problemImageCdnUrl,
    ActiveStatusType activeStatus,
    LocalDateTime createdAt
) {

    public static ProblemInfoResponseDto from(
        ProblemEntity problem,
        GymEntity gym,
        GymAreaEntity gymArea
    ) {
        return ProblemInfoResponseDto.builder()
            .problemId(problem.problemId())
            .gymId(gym.gymId())
            .gymName(gym.name())
            .gymAreaId(gymArea.gymAreaId())
            .gymAreaName(gymArea.areaName())
            .localLevel(problem.localLevel())
            .holdColor(problem.holdColor())
            .problemRating(problem.problemRating())
            .problemTier(problem.problemTier())
            .problemImageCdnUrl(problem.problemImageCdnUrl())
            .activeStatus(problem.activeStatus())
            .createdAt(problem.createdAt())
            .build();
    }
}
