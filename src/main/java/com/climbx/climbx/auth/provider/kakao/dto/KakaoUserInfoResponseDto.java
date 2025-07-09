package com.climbx.climbx.auth.provider.kakao.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserInfoResponseDto(

    Long id,
    String connectedAt,
    KakaoAccount kakaoAccount
) {

    public record KakaoAccount(

        Boolean profileNicknameNeedsAgreement,
        Boolean hasEmail,
        Boolean emailNeedsAgreement,
        Boolean isEmailValid,
        Boolean isEmailVerified,
        String email,
        Profile profile
    ) {

    }

    public record Profile(

        String nickname,
        String profileImageUrl,
        String thumbnailImageUrl,
        Boolean isDefaultImage
    ) {

    }
} 