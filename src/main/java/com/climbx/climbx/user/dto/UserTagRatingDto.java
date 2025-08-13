package com.climbx.climbx.user.dto;

import com.climbx.climbx.problem.enums.ProblemTagType;

public record UserTagRatingDto(

    Long userId,
    ProblemTagType tag,
    Integer rating
) {

}