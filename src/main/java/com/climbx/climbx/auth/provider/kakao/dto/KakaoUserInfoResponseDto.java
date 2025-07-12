package com.climbx.climbx.auth.provider.kakao.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserInfoResponseDto(

    Long id,
    String connectedAt,
    KakaoAccount kakaoAccount
) {

    public static String getNickname(KakaoUserInfoResponseDto kakaoUser) {
        return kakaoUser.kakaoAccount().profile().nickname();
    }

    public static String getProfileImageUrl(KakaoUserInfoResponseDto kakaoUser) {
        return kakaoUser.kakaoAccount().profile().profileImageUrl();
    }

    public static String getEmail(KakaoUserInfoResponseDto kakaoUser) {
        return kakaoUser.kakaoAccount().email();
    }

    public static Boolean isEmailVerified(KakaoUserInfoResponseDto kakaoUser) {
        return kakaoUser.kakaoAccount().isEmailVerified();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Profile(

        String nickname,
        String profileImageUrl,
        String thumbnailImageUrl,
        Boolean isDefaultImage
    ) {

    }
} 