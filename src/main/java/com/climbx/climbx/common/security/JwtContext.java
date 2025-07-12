package com.climbx.climbx.common.security;

import com.climbx.climbx.common.comcode.ComcodeService;
import com.climbx.climbx.common.security.exception.InvalidTokenException;
import com.climbx.climbx.common.security.exception.TokenExpiredException;
import com.climbx.climbx.common.util.OptionalUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
public class JwtContext {

    private final ComcodeService comcodeService;
    private final BearerTokenResolver bearerTokenResolver;
    private final String jwtSecret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;
    private final Key signingKey;

    public JwtContext(
        ComcodeService comcodeService,
        @Value("${auth.jwt.secret}") String jwtSecret,
        @Value("${auth.jwt.access-token-expiration}") long accessTokenExpiration,
        @Value("${auth.jwt.refresh-token-expiration}") long refreshTokenExpiration,
        @Value("${auth.jwt.issuer}") String issuer
    ) {
        this.comcodeService = comcodeService;

        // DefaultBearerTokenResolver 설정
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        resolver.setAllowFormEncodedBodyParameter(false); // Form parameter 비활성화
        resolver.setAllowUriQueryParameter(false); // Query parameter 비활성화 (보안상 권장)
        this.bearerTokenResolver = resolver;

        this.jwtSecret = jwtSecret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Bearer 토큰 사용 Spring Security DefaultBearerTokenResolver 사용
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(bearerTokenResolver.resolve(request))
            .orElseThrow(
                () -> new InvalidTokenException("Bearer token not found in request header"));
    }

    public String generateAccessToken(Long userId, String provider, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration * 1000);

        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("provider", provider)
            .claim("role", role)
            .claim("type", comcodeService.getCodeValue("ACCESS"))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(Long userId, String provider) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration * 1000);

        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("provider", provider)
            .claim("type", comcodeService.getCodeValue("REFRESH"))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 토큰 Payload 추출
     */
    private Claims extractClaims(String token) {
        if (token == null) {
            throw new InvalidTokenException("Token is null");
        }

        try {
            return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (Exception e) {
            throw new InvalidTokenException();
        }
    }

    /**
     * 사용자 ID 추출
     */
    public Long extractSubject(String token) {
        return OptionalUtils.tryOf(
                () -> {
                    Claims claims = extractClaims(token);
                    String subject = claims.getSubject();
                    return Long.parseLong(subject);
                }
            )
            .orElseThrow(() -> new InvalidTokenException("user id not found in payload"));
    }

    /**
     * 토큰 타입을 추출합니다 (access 또는 refresh)
     */
    public String extractTokenType(String token) {
        return OptionalUtils.tryOf(
                () -> {
                    Claims claims = extractClaims(token);
                    String type = claims.get("type", String.class);
                    return comcodeService.getCodeValue(type);
                }
            )
            .orElseThrow(() -> new InvalidTokenException("Valid token type not found in payload"));
    }

    /**
     * 토큰 Provider 추출
     */
    public String extractProvider(String token) {
        return OptionalUtils.tryOf(
                () -> {
                    Claims claims = extractClaims(token);
                    return claims.get("provider", String.class);
                }
            )
            .orElseThrow(() -> new InvalidTokenException("Valid provider not found in payload"));
    }

    /**
     * 토큰 role 추출
     */
    public String extractRole(String token) {
        return OptionalUtils.tryOf(
                () -> {
                    Claims claims = extractClaims(token);
                    String role = claims.get("role", String.class);
                    return comcodeService.getCodeValue(role);
                }
            )
            .orElseThrow(() -> new InvalidTokenException("Valid role not found in payload"));
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}