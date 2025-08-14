package com.climbx.climbx.user.dto;

import lombok.Builder;

@Builder
public record TagRatingResponseDto(

    String category, // ProglemTag.displayName()
    Integer rating
) {

}
