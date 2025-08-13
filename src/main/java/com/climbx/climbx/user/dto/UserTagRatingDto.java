package com.climbx.climbx.user.dto;

import lombok.Builder;

@Builder
public record UserTagRatingDto(

    Long userId,
    String tag,
    Integer rating
) {

}