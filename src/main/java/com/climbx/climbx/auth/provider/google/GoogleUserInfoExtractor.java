package com.climbx.climbx.auth.provider.google;

import com.climbx.climbx.auth.dto.ValidatedTokenInfoDto;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.provider.UserInfoExtractor;
import com.climbx.climbx.auth.provider.exception.EmailNotVerifiedException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleUserInfoExtractor implements UserInfoExtractor {

    @Value("${spring.security.oauth2.google.jwks-uri}")
    private String jwksUri;

    @Value("${spring.security.oauth2.google.default-issuer}")
    private String defaultIssuer;

    @Value("${spring.security.oauth2.google.http-issuer}")
    private String httpIssuer;

    @Value("${spring.security.oauth2.google.android-audience}")
    private String androidAudience;

    @Value("${spring.security.oauth2.google.ios-audience}")
    private String iosAudience;

    @Override
    public OAuth2ProviderType getProviderType() {
        return OAuth2ProviderType.GOOGLE;
    }

    @Override
    public String getJwksUri() {
        return jwksUri;
    }

    @Override
    public List<String> getIssuer() {
        return List.of(
            defaultIssuer,
            httpIssuer
        );
    }

    @Override
    public List<String> getAudience() {
        return List.of(
            androidAudience,
            iosAudience
        );
    }

    @Override
    public boolean nonceCheckEnabled() {
        return false;
    }

    @Override
    public ValidatedTokenInfoDto extractUserInfo(Jwt jwt) {
        log.debug("Google ID Token에서 사용자 정보 추출 시작");

        String providerId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String nickname = jwt.getClaimAsString("name");
        String profileImageUrl = jwt.getClaimAsString("picture");

        boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        if (!emailVerified) {
            log.warn("Google ID Token에서 이메일이 인증되지 않았습니다: providerId={}, email={}", providerId,
                email);
            throw new EmailNotVerifiedException(OAuth2ProviderType.GOOGLE);
        }

        log.debug(
            "Google 사용자 정보 추출 완료: providerId={}, email={}, nickname={}",
            providerId,
            email,
            nickname
        );

        return ValidatedTokenInfoDto.builder()
            .providerId(providerId)
            .nickname(nickname)
            .email(email)
            .profileImageUrl(profileImageUrl)
            .providerType(OAuth2ProviderType.GOOGLE)
            .build();
    }
}
