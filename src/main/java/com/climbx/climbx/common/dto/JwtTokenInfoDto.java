package com.climbx.climbx.common.dto;

import com.climbx.climbx.common.enums.TokenType;
import lombok.Builder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * JWT 토큰 파싱 결과를 담는 DTO
 */
@Builder
public record JwtTokenInfoDto(

    Long userId,
    String issuer,
    String audience,
    String role,
    TokenType tokenType
) {

    public static JwtTokenInfoDto from(Jwt jwt) {
        return JwtTokenInfoDto.builder()
            .userId(Long.parseLong(jwt.getSubject()))
            .issuer(jwt.getIssuer().toString())
            .audience(jwt.getAudience().stream().findFirst().orElse(null))
            .role(jwt.getClaimAsString("role"))
            .tokenType(TokenType.from(jwt.getClaimAsString("type")))
            .build();
    }
} 