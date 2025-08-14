package com.climbx.climbx.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.climbx.climbx.common.exception.InvalidTokenException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenBlacklistService 테스트")
class RefreshTokenBlacklistServiceTest {

    @Mock
    private Cache<String, Boolean> refreshTokenBlacklist;

    @Mock
    private CacheStats cacheStats;

    @InjectMocks
    private RefreshTokenBlacklistService refreshTokenBlacklistService;

    @Nested
    @DisplayName("토큰 블랙리스트 검증 테스트")
    class ValidateTokenNotBlacklistedTest {

        @Test
        @DisplayName("유효한 토큰이 블랙리스트에 없으면 통과한다")
        void shouldPassWhenValidTokenNotInBlacklist() {
            // given
            String validToken = "valid.refresh.token";
            given(refreshTokenBlacklist.getIfPresent(validToken)).willReturn(null);

            // when & then - 예외가 발생하지 않아야 함
            refreshTokenBlacklistService.validateTokenNotBlacklisted(validToken);

            then(refreshTokenBlacklist).should().getIfPresent(validToken);
        }

        @Test
        @DisplayName("블랙리스트에 등록된 토큰으로 검증 시 예외를 던진다")
        void shouldThrowExceptionWhenTokenInBlacklist() {
            // given
            String blacklistedToken = "blacklisted.refresh.token";
            given(refreshTokenBlacklist.getIfPresent(blacklistedToken)).willReturn(true);

            // when & then
            assertThatThrownBy(
                () -> refreshTokenBlacklistService.validateTokenNotBlacklisted(blacklistedToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효하지 않은 토큰입니다.");

            then(refreshTokenBlacklist).should().getIfPresent(blacklistedToken);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("null, 빈 문자열, 공백만 있는 토큰으로 검증 시 InvalidTokenException을 던진다")
        void shouldThrowInvalidTokenExceptionWhenTokenIsInvalid(String token) {
            // when & then
            assertThatThrownBy(
                () -> refreshTokenBlacklistService.validateTokenNotBlacklisted(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효하지 않은 토큰입니다.");

            then(refreshTokenBlacklist).should(never()).getIfPresent(anyString());
        }
    }

    @Nested
    @DisplayName("토큰 블랙리스트 추가 테스트")
    class AddToBlacklistTest {

        @Test
        @DisplayName("유효한 토큰을 블랙리스트에 성공적으로 추가한다")
        void shouldAddValidTokenToBlacklist() {
            // given
            String token = "refresh.token.to.blacklist";

            // when
            refreshTokenBlacklistService.addToBlacklist(token);

            // then
            then(refreshTokenBlacklist).should().put(token, true);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("null, 빈 문자열, 공백만 있는 토큰 추가 시 캐시에 추가하지 않는다")
        void shouldNotAddInvalidTokenToBlacklist(String token) {
            // when
            refreshTokenBlacklistService.addToBlacklist(token);

            // then
            then(refreshTokenBlacklist).should(never()).put(anyString(), any(Boolean.class));
        }
    }

    @Nested
    @DisplayName("모니터링 메서드 테스트")
    class MonitoringMethodsTest {

        @Test
        @DisplayName("블랙리스트 크기를 정확히 반환한다")
        void shouldReturnCorrectBlacklistSize() {
            // given
            long expectedSize = 10L;
            given(refreshTokenBlacklist.estimatedSize()).willReturn(expectedSize);

            // when
            long actualSize = refreshTokenBlacklistService.getBlacklistSize();

            // then
            assertThat(actualSize).isEqualTo(expectedSize);
            then(refreshTokenBlacklist).should().estimatedSize();
        }

        @Test
        @DisplayName("캐시 통계를 정확히 반환한다")
        void shouldReturnCorrectCacheStats() {
            // given
            String expectedStats = "CacheStats{hitCount=100, missCount=10}";
            given(refreshTokenBlacklist.stats()).willReturn(cacheStats);
            given(cacheStats.toString()).willReturn(expectedStats);

            // when
            String actualStats = refreshTokenBlacklistService.getCacheStats();

            // then
            assertThat(actualStats).isEqualTo(expectedStats);
            then(refreshTokenBlacklist).should().stats();
        }
    }
} 