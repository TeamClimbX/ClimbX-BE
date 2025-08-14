package com.climbx.climbx.auth.provider.kakao;

import com.climbx.climbx.auth.dto.ValidatedTokenInfoDto;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.provider.UserInfoExtractor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Kakao OAuth2 Provider의 사용자 정보 추출 구현체
 */
@Slf4j
@Component
public class KakaoUserInfoExtractor implements UserInfoExtractor {

    @Value("${spring.security.oauth2.kakao.jwks-uri}")
    private String jwksUri;

    @Value("${spring.security.oauth2.kakao.issuer}")
    private String issuer;

    @Value("${spring.security.oauth2.kakao.audience}")
    private String audience;

    @Override
    public OAuth2ProviderType getProviderType() {
        return OAuth2ProviderType.KAKAO;
    }

    @Override
    public String getJwksUri() {
        return jwksUri;
    }

    @Override
    public List<String> getIssuer() {
        return List.of(
            issuer
        );
    }

    @Override
    public List<String> getAudience() {
        return List.of(
            audience
        );
    }

    @Override
    public boolean nonceCheckEnabled() {
        return true;
    }

    @Override
    public ValidatedTokenInfoDto extractUserInfo(Jwt jwt) {
        log.debug("Kakao ID Token에서 사용자 정보 추출 시작");

        String providerId = jwt.getSubject(); // sub 클레임
        String email = jwt.getClaimAsString("email");
        String nickname = jwt.getClaimAsString("nickname");
        String profileImageUrl = jwt.getClaimAsString("picture");

        log.debug(
            "Kakao 사용자 정보 추출 완료: providerId={}, email={}, nickname={}",
            providerId,
            email,
            nickname
        );

        return ValidatedTokenInfoDto.builder()
            .providerId(providerId)
            .nickname(nickname)
            .email(email)
            .profileImageUrl(profileImageUrl)
            .providerType(OAuth2ProviderType.KAKAO)
            .build();
    }
} 