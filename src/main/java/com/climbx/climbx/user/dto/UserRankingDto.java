package com.climbx.climbx.user.dto;

import lombok.Builder;

@Builder
public record UserRankingDto(

    Long userId,
    Integer ranking
) {

}