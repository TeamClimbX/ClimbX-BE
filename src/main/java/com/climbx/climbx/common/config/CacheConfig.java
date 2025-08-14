package com.climbx.climbx.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 사용된 Refresh Token 블랙리스트 캐시 TTL: 7일 (일반적인 refresh token 만료 시간) 최대 크기: 100,000개
     */
    @Bean
    public Cache<String, Boolean> refreshTokenBlacklist() {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(7))
            .maximumSize(100000)
            .recordStats() // 모니터링용 통계 기록
            .build();
    }

    /**
     * 사용된 Nonce 캐시 TTL: 10분 (OAuth2 인증 플로우 완료에 충분한 시간) 최대 크기: 50,000개
     */
    @Bean
    public Cache<String, Boolean> usedNonces() {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(50000)
            .recordStats()
            .build();
    }

    @Bean
    public Cache<String, Boolean> existingBuckets() {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(7))
            .maximumSize(100)
            .recordStats()
            .build();
    }
} 