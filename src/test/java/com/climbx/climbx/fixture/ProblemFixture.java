package com.climbx.climbx.fixture;

import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.entity.ProblemEntity;
import java.util.UUID;

public class ProblemFixture {

    public static final Integer DEFAULT_PROBLEM_RATING = 1200;
    public static final String DEFAULT_LOCAL_LEVEL = "빨강";
    public static final String DEFAULT_HOLD_COLOR = "파랑";
    public static final String DEFAULT_IMAGE_URL = "http://example.com/image.jpg";

    public static ProblemEntity createProblemEntity(UUID problemId, GymEntity gym,
        GymAreaEntity gymArea) {
        return createProblemEntity(problemId, gym, gymArea, DEFAULT_LOCAL_LEVEL, DEFAULT_HOLD_COLOR,
            DEFAULT_PROBLEM_RATING);
    }

    public static ProblemEntity createProblemEntity(
        UUID problemId,
        GymEntity gym,
        GymAreaEntity gymArea,
        String localLevel,
        String holdColor,
        Integer problemRating
    ) {
        return ProblemEntity.builder()
            .problemId(problemId)
            .gymEntity(gym)
            .gymArea(gymArea)
            .localLevel(localLevel)
            .holdColor(holdColor)
            .rating(problemRating)
            .problemImageCdnUrl(DEFAULT_IMAGE_URL)
            .build();
    }

    public static ProblemInfoResponseDto createProblemResponseDto(
        UUID problemId,
        Long gymId,
        String gymName,
        Long gymAreaId,
        String gymAreaName,
        String localLevel,
        String holdColor,
        Integer rating
    ) {
        return ProblemInfoResponseDto.builder()
            .problemId(problemId)
            .gymId(gymId)
            .gymName(gymName)
            .gymAreaId(gymAreaId)
            .gymAreaName(gymAreaName)
            .localLevel(localLevel)
            .holdColor(holdColor)
            .rating(rating)
            .problemImageCdnUrl(DEFAULT_IMAGE_URL)
            .build();
    }
} 