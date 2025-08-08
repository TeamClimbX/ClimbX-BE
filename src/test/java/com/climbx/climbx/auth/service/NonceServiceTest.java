package com.climbx.climbx.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.provider.exception.InvalidNonceException;
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
@DisplayName("NonceService 테스트")
class NonceServiceTest {

    @Mock
    private Cache<String, Boolean> usedNonces;

    @Mock
    private CacheStats cacheStats;

    @InjectMocks
    private NonceService nonceService;

    @Nested
    @DisplayName("Nonce 검증 및 사용 테스트")
    class ValidateAndUseNonceTest {

        @Test
        @DisplayName("새로운 nonce를 성공적으로 검증하고 사용 등록한다")
        void shouldValidateAndUseNewNonce() {
            // given
            String newNonce = "new-unique-nonce-12345";
            given(usedNonces.getIfPresent(newNonce)).willReturn(null);

            // when
            nonceService.validateAndUseNonce(newNonce, OAuth2ProviderType.KAKAO);

            // then
            then(usedNonces).should().getIfPresent(newNonce);
            then(usedNonces).should().put(newNonce, true);
        }

        @Test
        @DisplayName("이미 사용된 nonce로 검증 시 InvalidNonceException을 던진다")
        void shouldThrowInvalidNonceExceptionWhenNonceAlreadyUsed() {
            // given
            String usedNonce = "already-used-nonce-12345";
            given(usedNonces.getIfPresent(usedNonce)).willReturn(true);

            // when & then
            assertThatThrownBy(
                () -> nonceService.validateAndUseNonce(usedNonce, OAuth2ProviderType.KAKAO))
                .isInstanceOf(InvalidNonceException.class);

            then(usedNonces).should().getIfPresent(usedNonce);
            then(usedNonces).should(never()).put(anyString(), any(Boolean.class));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("null, 빈 문자열, 공백만 있는 nonce로 검증 시 IllegalArgumentException을 던진다")
        void shouldThrowIllegalArgumentExceptionWhenNonceIsInvalid(String nonce) {
            // when & then
            assertThatThrownBy(() -> nonceService.validateAndUseNonce(nonce, OAuth2ProviderType.KAKAO))
                .isInstanceOf(InvalidNonceException.class);

            then(usedNonces).should(never()).getIfPresent(anyString());
            then(usedNonces).should(never()).put(anyString(), any(Boolean.class));
        }

        @Test
        @DisplayName("같은 nonce를 두 번 사용하려고 하면 두 번째에 예외를 던진다")
        void shouldThrowExceptionWhenUsingSameNonceTwice() {
            // given
            String nonce = "test-nonce-12345";

            // 첫 번째 호출 시 - 새로운 nonce
            given(usedNonces.getIfPresent(nonce)).willReturn(null);

            // when - 첫 번째 사용은 성공
            nonceService.validateAndUseNonce(nonce, OAuth2ProviderType.KAKAO);

            // given - 두 번째 호출 시 - 이미 사용된 nonce
            given(usedNonces.getIfPresent(nonce)).willReturn(true);

            // when & then - 두 번째 사용은 예외 발생
            assertThatThrownBy(
                () -> nonceService.validateAndUseNonce(nonce, OAuth2ProviderType.KAKAO))
                .isInstanceOf(InvalidNonceException.class);

            then(usedNonces).should().put(nonce, true);
        }
    }

    @Nested
    @DisplayName("모니터링 메서드 테스트")
    class MonitoringMethodsTest {

        @Test
        @DisplayName("사용된 nonce 수를 정확히 반환한다")
        void shouldReturnCorrectUsedNonceCount() {
            // given
            long expectedCount = 25L;
            given(usedNonces.estimatedSize()).willReturn(expectedCount);

            // when
            long actualCount = nonceService.getUsedNonceCount();

            // then
            assertThat(actualCount).isEqualTo(expectedCount);
            then(usedNonces).should().estimatedSize();
        }

        @Test
        @DisplayName("캐시 통계를 정확히 반환한다")
        void shouldReturnCorrectCacheStats() {
            // given
            String expectedStats = "CacheStats{hitCount=50, missCount=5}";
            given(usedNonces.stats()).willReturn(cacheStats);
            given(cacheStats.toString()).willReturn(expectedStats);

            // when
            String actualStats = nonceService.getCacheStats();

            // then
            assertThat(actualStats).isEqualTo(expectedStats);
            then(usedNonces).should().stats();
        }

        @Test
        @DisplayName("빈 캐시에서 nonce 수를 0으로 반환한다")
        void shouldReturnZeroForEmptyCache() {
            // given
            given(usedNonces.estimatedSize()).willReturn(0L);

            // when
            long count = nonceService.getUsedNonceCount();

            // then
            assertThat(count).isZero();
        }
    }
} 