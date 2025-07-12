package com.climbx.climbx.auth.provider.kakao.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoTokenResponseDto(

    String tokenType,
    String accessToken,
    String idToken, // OpenID Connect 토큰
    Integer expiresIn,
    String refreshToken,
    Integer refreshTokenExpiresIn,
    String scope
) {

}