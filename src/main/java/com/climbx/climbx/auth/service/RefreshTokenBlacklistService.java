package com.climbx.climbx.auth.service;

import com.climbx.climbx.common.exception.InvalidTokenException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenBlacklistService {

    private final Cache<String, Boolean> refreshTokenBlacklist;

    /**
     * 리프레시 토큰이 블랙리스트에 있는지 확인합니다.
     *
     * @param refreshToken 검증할 리프레시 토큰
     * @throws InvalidTokenException 블랙리스트에 등록된 토큰인 경우
     */
    public void validateTokenNotBlacklisted(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            log.warn("empty refresh token 사용 시도");
            throw new InvalidTokenException("refresh token is empty");
        }

        if (refreshTokenBlacklist.getIfPresent(refreshToken) != null) {
            log.warn("블랙리스트에 등록된 리프레시 토큰 사용 시도: {}",
                refreshToken.substring(0, Math.min(refreshToken.length(), 20)) + "...");
            throw new InvalidTokenException("refresh token is blacklisted");
        }
    }

    /**
     * 리프레시 토큰을 블랙리스트에 추가합니다.
     *
     * @param refreshToken 블랙리스트에 추가할 리프레시 토큰
     */
    public void addToBlacklist(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            log.warn("빈 리프레시 토큰을 블랙리스트에 추가하려고 시도했습니다.");
            return;
        }

        refreshTokenBlacklist.put(refreshToken, true);

        log.debug("리프레시 토큰을 블랙리스트에 추가: {}",
            refreshToken.substring(0, Math.min(refreshToken.length(), 20)) + "...");
    }

    /**
     * 현재 블랙리스트에 등록된 토큰 수를 반환합니다. (모니터링용)
     */
    public long getBlacklistSize() {
        return refreshTokenBlacklist.estimatedSize();
    }

    /**
     * 블랙리스트 캐시 통계를 반환합니다. (모니터링용)
     */
    public String getCacheStats() {
        return refreshTokenBlacklist.stats().toString();
    }
} 