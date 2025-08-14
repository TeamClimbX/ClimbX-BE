package com.climbx.climbx.auth.dto;

import lombok.Builder;

@Builder
public record TokenGenerationResponseDto(

    AccessTokenResponseDto accessToken,
    String refreshToken
) {

    public static TokenGenerationResponseDto from(AccessTokenResponseDto accessToken,
        String refreshToken) {
        return TokenGenerationResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
