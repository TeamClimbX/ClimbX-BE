package com.climbx.climbx.auth.dto;

import lombok.Builder;

@Builder
public record AccessTokenResponseDto(

    String accessToken, // JWT token
    Long expiresIn // 토큰 만료 시간 (초 단위)
) {

}
