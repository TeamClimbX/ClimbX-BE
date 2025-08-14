package com.climbx.climbx.auth.provider;

import com.climbx.climbx.auth.dto.ValidatedTokenInfoDto;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.provider.exception.InvalidNonceException;
import com.climbx.climbx.auth.service.NonceService;
import com.climbx.climbx.common.exception.InvalidTokenException;
import com.climbx.climbx.common.exception.TokenExpiredException;
import com.climbx.climbx.common.util.OptionalUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

/**
 * OAuth2 ID Token 검증 서비스 Spring Security의 JwtDecoder를 활용하여 외부 OIDC Provider의 ID Token을 검증합니다.
 */
@Slf4j
@Service
public class ProviderIdTokenService {

    private final Map<OAuth2ProviderType, JwtDecoder> idTokenDecoders;
    private final Map<OAuth2ProviderType, UserInfoExtractor> extractorMap;
    private final NonceService nonceService;

    /**
     * UserInfoExtractor 목록을 Map으로 변환하여 빠른 조회 지원
     */
    public ProviderIdTokenService(
        @Qualifier("oauth2IdTokenDecoders") Map<OAuth2ProviderType, JwtDecoder> idTokenDecoders,
        List<UserInfoExtractor> userInfoExtractors,
        NonceService nonceService
    ) {
        this.idTokenDecoders = idTokenDecoders;
        this.extractorMap = userInfoExtractors.stream()
            .collect(Collectors.toMap(UserInfoExtractor::getProviderType, Function.identity()));
        this.nonceService = nonceService;

        log.info("OAuth2IdTokenService 초기화 완료. 지원 Provider: {}", extractorMap.keySet());
    }

    /**
     * 지정된 Provider의 ID Token을 검증하고 사용자 정보를 추출합니다.
     *
     * @param providerType Provider 타입 (kakao, google, apple)
     * @param idToken      검증할 ID Token
     * @param nonce        OIDC nonce 값 (CSRF 공격 방지)
     * @return 검증된 사용자 정보
     */
    public ValidatedTokenInfoDto verifyIdToken(
        OAuth2ProviderType providerType,
        String idToken,
        String nonce
    ) {

        try {
            log.debug("{} ID Token 검증 시작", providerType);

            // Provider별 JwtDecoder 선택
            JwtDecoder decoder = getJwtDecoder(providerType);

            Jwt jwt = decoder.decode(idToken);

            // nonce 검증 (CSRF 공격 방지)
            validateNonce(jwt, nonce, providerType);

            // Provider별 사용자 정보 추출
            UserInfoExtractor extractor = getExtractor(providerType);
            ValidatedTokenInfoDto userInfo = extractor.extractUserInfo(jwt);

            log.info("{} ID Token 검증 성공: providerId={}, email={}",
                providerType.name(), userInfo.providerId(), userInfo.email());

            return userInfo;

        } catch (BadJwtException e) {
            if (e.getMessage().contains("expired")) {
                log.warn("{} ID Token 만료: {}", providerType.name(), e.getMessage());
                throw new TokenExpiredException();
            }
            log.warn("{} ID Token Invalid", providerType.name(), e);
            throw new InvalidTokenException(e.getMessage());
        } catch (Exception e) {
            log.warn("{} ID Token Invalid", providerType.name(), e);
            throw new InvalidTokenException(e.getMessage());
        }
    }

    /**
     * Provider에 해당하는 JwtDecoder를 가져옵니다.
     */
    private JwtDecoder getJwtDecoder(OAuth2ProviderType providerType) {
        return Optional.ofNullable(idTokenDecoders.get(providerType))
            .orElseThrow(() -> new InvalidTokenException(
                "provider decoder not found for " + providerType.name()
            ));
    }

    /**
     * Provider에 해당하는 UserInfoExtractor를 가져옵니다.
     */
    private UserInfoExtractor getExtractor(OAuth2ProviderType providerType) {
        return Optional.ofNullable(extractorMap.get(providerType))
            .orElseThrow(() -> new InvalidTokenException(
                "provider extractor not found for " + providerType.name()
            ));
    }

    /**
     * nonce 클레임을 검증합니다.
     */
    private void validateNonce(Jwt jwt, String expectedNonce, OAuth2ProviderType providerType) {
        if (!getExtractor(providerType).nonceCheckEnabled()) {
            return;
        }

        OptionalUtil.tryOf(() -> jwt.getClaimAsString("nonce"))
            .filter(nonce -> !nonce.isBlank() && nonce.equals(expectedNonce))
            .ifPresentOrElse(nonce -> nonceService.validateAndUseNonce(nonce, providerType), () -> {
                log.warn(
                    "{} ID Token validation failed: nonce={}", providerType.name(), expectedNonce
                );
                throw new InvalidNonceException(providerType);
            });
    }
} 