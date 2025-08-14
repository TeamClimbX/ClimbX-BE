package com.climbx.climbx.user.dto;

import lombok.Builder;

@Builder
public record RatingResponseDto(

    Integer totalRating,
    Integer topProblemRating,
    Integer solvedRating,
    Integer submissionRating,
    Integer contributionRating
) {

}
