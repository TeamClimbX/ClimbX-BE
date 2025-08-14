package com.climbx.climbx.auth.provider;

import com.climbx.climbx.auth.dto.ValidatedTokenInfoDto;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * OAuth2 Provider별 사용자 정보 추출 인터페이스
 */
public interface UserInfoExtractor {

    /**
     * 지원하는 Provider 타입을 반환합니다.
     */
    OAuth2ProviderType getProviderType();

    /**
     * Provider의 JWKS URI를 반환합니다.
     */
    String getJwksUri();

    /**
     * Provider의 Issuer를 반환합니다.
     */
    List<String> getIssuer();

    /**
     * Provider의 Audience를 반환합니다.
     */
    List<String> getAudience();

    boolean nonceCheckEnabled();

    /**
     * JWT에서 사용자 정보를 추출합니다.
     *
     * @param jwt 검증된 JWT 토큰
     * @return 추출된 사용자 정보
     */
    ValidatedTokenInfoDto extractUserInfo(Jwt jwt);
} 