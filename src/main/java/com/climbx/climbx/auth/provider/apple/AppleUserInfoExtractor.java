package com.climbx.climbx.auth.provider.apple;

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
public class AppleUserInfoExtractor implements UserInfoExtractor {

    @Value("${spring.security.oauth2.apple.jwks-uri}")
    private String jwksUri;

    @Value("${spring.security.oauth2.apple.issuer}")
    private String issuer;

    @Value("${spring.security.oauth2.apple.android-audience}")
    private String androidAudience;

    @Value("${spring.security.oauth2.apple.ios-audience}")
    private String iosAudience;

    @Override
    public OAuth2ProviderType getProviderType() {
        return OAuth2ProviderType.APPLE;
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
            androidAudience,
            iosAudience
        );
    }

    @Override
    public boolean nonceCheckEnabled() {
        return true;
    }

    @Override
    public ValidatedTokenInfoDto extractUserInfo(Jwt jwt) {
        log.debug("Apple ID Token에서 사용자 정보 추출 시작");

        String providerId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String nickname = null;
        String profileImageUrl = null;

        boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        if (!emailVerified) {
            log.warn("Apple ID Token에서 이메일이 인증되지 않았습니다: providerId={}, email={}", providerId,
                email);
            throw new EmailNotVerifiedException(OAuth2ProviderType.APPLE);
        }

        log.debug(
            "Apple 사용자 정보 추출 완료: providerId={}, email={}, nickname={}",
            providerId,
            email,
            nickname
        );

        return ValidatedTokenInfoDto.builder()
            .providerId(providerId)
            .nickname(nickname)
            .email(email)
            .profileImageUrl(profileImageUrl)
            .providerType(OAuth2ProviderType.APPLE)
            .build();
    }
}