package com.climbx.climbx.common.config;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.provider.UserInfoExtractor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * OAuth2 ID Token 검증을 위한 Configuration Spring Security OAuth2 Resource Server 기반
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProviderIdTokenConfig {

    private final List<UserInfoExtractor> userInfoExtractors;

    /**
     * Provider별 JwtDecoder 맵을 생성합니다. 각 Provider의 구현체에서 제공하는 설정을 사용하여 JwtDecoder를 동적으로 생성합니다.
     */
    @Bean
    public Map<OAuth2ProviderType, JwtDecoder> oauth2IdTokenDecoders() {
        log.info("OAuth2 ID Token Decoders 초기화 시작");

        Map<OAuth2ProviderType, JwtDecoder> decoders = userInfoExtractors.stream()
            .collect(Collectors.toMap(
                UserInfoExtractor::getProviderType,
                this::createJwtDecoder
            ));

        log.info("OAuth2 ID Token Decoders 초기화 완료: {}", decoders.keySet());
        return decoders;
    }

    /**
     * UserInfoExtractor에서 제공하는 설정을 사용하여 JwtDecoder를 생성합니다. NimbusJwtDecoder가 공개키 캐싱 및 자동 갱신 기능 제공
     */
    private JwtDecoder createJwtDecoder(UserInfoExtractor extractor) {
        String providerType = extractor.getProviderType().name();
        String jwksUri = extractor.getJwksUri();
        List<String> issuers = extractor.getIssuer();
        List<String> audiences = extractor.getAudience();

        log.debug("{} JwtDecoder 생성 시작: jwksUri={}, issuer={}", providerType, jwksUri,
            issuers.toString());

        // NimbusJwtDecoder를 직접 생성하여 커스텀 검증기 설정 가능
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withJwkSetUri(jwksUri)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();

        // 기본 검증 - exp, nbf 등
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefault();

        OAuth2TokenValidator<Jwt> issuerValidator = createMultiIssuerValidator(issuers);
        OAuth2TokenValidator<Jwt> audienceValidator = createMultiAudienceValidator(audiences);

        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
            defaultValidators,
            issuerValidator,
            audienceValidator
        );

        decoder.setJwtValidator(combinedValidator);

        log.info("{} JwtDecoder 생성 완료", providerType);
        return decoder;
    }

    /**
     * 여러 issuer를 지원하는 커스텀 Validator 생성 List에 포함된 issuer 중 하나라도 일치하면 검증 통과
     */
    private OAuth2TokenValidator<Jwt> createMultiIssuerValidator(List<String> expectedIssuers) {
        return jwt -> {
            String actualIssuer = jwt.getIssuer().toString();

            if (expectedIssuers.contains(actualIssuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(
                    "invalid_issuer",
                    "The required issuer is missing. Expected one of: "
                        + expectedIssuers + ", but was: " + actualIssuer,
                    null
                )
            );
        };
    }

    /**
     * 여러 audience를 지원하는 커스텀 Validator 생성 List에 포함된 audience 중 하나라도 일치하면 검증 통과
     */
    private OAuth2TokenValidator<Jwt> createMultiAudienceValidator(List<String> expectedAudiences) {
        return jwt -> {
            List<String> actualAudiences = jwt.getAudience();

            if (actualAudiences != null) {
                // JWT의 audience 목록과 기대하는 audience 목록이 교집합을 가지는지 확인
                for (String expectedAudience : expectedAudiences) {
                    if (actualAudiences.contains(expectedAudience)) {
                        return OAuth2TokenValidatorResult.success();
                    }
                }
            }

            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(
                    "invalid_audience",
                    "The required audience is missing. Expected one of: "
                        + expectedAudiences + ", but was: " + actualAudiences,
                    null
                )
            );
        };
    }
} 