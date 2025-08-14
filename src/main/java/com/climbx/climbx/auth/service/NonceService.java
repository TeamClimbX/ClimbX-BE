package com.climbx.climbx.auth.service;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.provider.exception.InvalidNonceException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NonceService {

    private final Cache<String, Boolean> usedNonces;

    /**
     * Nonce를 검증하고 일회성 사용을 보장합니다.
     *
     * @param nonce 검증할 nonce
     * @throws IllegalArgumentException nonce가 null이거나 빈 문자열인 경우
     * @throws InvalidNonceException    이미 사용된 nonce인 경우
     */
    public void validateAndUseNonce(String nonce, OAuth2ProviderType providerType) {
        if (nonce == null || nonce.trim().isEmpty()) {
            log.warn("비어있는 nonce 감지");
            throw new InvalidNonceException(providerType);
        }

        // 이미 사용된 nonce인지 확인
        if (usedNonces.getIfPresent(nonce) != null) {
            log.warn("이미 사용된 nonce 감지: {}", nonce);
            throw new InvalidNonceException(providerType); // 기본값 사용
        }

        // 사용된 nonce로 등록
        usedNonces.put(nonce, true);
        log.debug("Nonce 사용 등록: {}", nonce);
    }

    /**
     * 현재 캐시에 저장된 nonce 수를 반환합니다. (모니터링용)
     */
    public long getUsedNonceCount() {
        return usedNonces.estimatedSize();
    }

    /**
     * 캐시 통계를 반환합니다. (모니터링용)
     */
    public String getCacheStats() {
        return usedNonces.stats().toString();
    }
} 