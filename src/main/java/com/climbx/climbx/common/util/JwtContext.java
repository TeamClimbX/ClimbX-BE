package com.climbx.climbx.common.util;

import com.climbx.climbx.auth.dto.AccessTokenResponseDto;
import com.climbx.climbx.common.dto.JwtTokenInfoDto;
import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.enums.TokenType;
import com.climbx.climbx.common.exception.InvalidTokenException;
import com.climbx.climbx.common.exception.TokenExpiredException;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtContext {

    private final BearerTokenResolver bearerTokenResolver;
    private final NimbusJwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;
    private final String audience;
    private final MacAlgorithm jwsAlgorithm;
    private final Function<JwtClaimsSet, String> tokenEncoder;

    public JwtContext(
        @Value("${auth.jwt.secret}") String jwtSecret,
        @Value("${auth.jwt.access-token-expiration}") long accessTokenExpiration,
        @Value("${auth.jwt.refresh-token-expiration}") long refreshTokenExpiration,
        @Value("${auth.jwt.issuer}") String issuer,
        @Value("${auth.jwt.audience}") String audience,
        @Value("${auth.jwt.jws-algorithm}") String jwsAlgorithm
    ) {
        // DefaultBearerTokenResolver 설정
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        resolver.setAllowFormEncodedBodyParameter(false); // Form parameter 비활성화
        resolver.setAllowUriQueryParameter(false); // Query parameter 비활성화 (보안상 권장)
        this.bearerTokenResolver = resolver;

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
        this.audience = audience;
        this.jwsAlgorithm = MacAlgorithm.from(jwsAlgorithm);

        // SecretKeySpec 생성
        SecretKeySpec secretKey = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8),
            jwsAlgorithm
        );

        // NimbusJwtDecoder 설정
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(secretKey)
            .build();

        // 표준 검증기 설정 (issuer 검증 포함)
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtAudienceValidator(audience);

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            defaultValidators,
            audienceValidator
        ));

        this.jwtDecoder = decoder;

        // NimbusJwtEncoder 설정 (토큰 생성용)
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));

        // 토큰 인코더 람다 (중복 코드 제거)
        this.tokenEncoder = claims -> {
            JwsHeader jwsHeader = JwsHeader.with(this.jwsAlgorithm)
                .type("JWT")
                .build();
            return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
        };
    }

    /**
     * Bearer 토큰 추출 - Spring Security DefaultBearerTokenResolver 사용
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(bearerTokenResolver.resolve(request))
            .orElseThrow(InvalidTokenException::new);
    }

    /**
     * Access Token 생성
     */
    public AccessTokenResponseDto generateAccessToken(Long userId, RoleType role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .audience(List.of(audience))
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("role", role.name())
            .claim("type", TokenType.ACCESS.name())
            .build();

        log.info("generating access token for userId: {}, role: {}", userId, role);

        return AccessTokenResponseDto.builder()
            .accessToken(tokenEncoder.apply(claims))
            .expiresIn(accessTokenExpiration)
            .build();
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(refreshTokenExpiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .audience(List.of(audience))
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("type", TokenType.REFRESH.name())
            .build();

        log.info("generating refresh token for userId: {}", userId);

        return tokenEncoder.apply(claims);
    }

    /**
     * 토큰에서 모든 정보를 한 번에 파싱
     */
    public JwtTokenInfoDto parseToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            return JwtTokenInfoDto.from(jwt);
        } catch (JwtException e) {
            // JwtException은 토큰 만료, 서명 오류 등을 포함
            if (e.getMessage().contains("expired")) {
                throw new TokenExpiredException();
            }
            throw new InvalidTokenException();
        }
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}