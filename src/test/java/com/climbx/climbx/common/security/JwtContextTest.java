package com.climbx.climbx.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.climbx.climbx.common.comcode.ComcodeService;
import com.climbx.climbx.common.security.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtContext 테스트")
class JwtContextTest {

    private static final String JWT_SECRET = "test-secret-key-for-jwt-token-generation-that-is-long-enough-to-meet-requirements";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600; // 1시간
    private static final long REFRESH_TOKEN_EXPIRATION = 86400; // 24시간
    private static final String ISSUER = "climbx-test";

    @Mock
    private ComcodeService comcodeService;

    @Mock
    private HttpServletRequest request;

    private JwtContext jwtContext;

    @BeforeEach
    void setUp() {
        jwtContext = new JwtContext(
            comcodeService,
            JWT_SECRET,
            ACCESS_TOKEN_EXPIRATION,
            REFRESH_TOKEN_EXPIRATION,
            ISSUER
        );
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class GenerateTokenTest {

        @Test
        @DisplayName("유효한 파라미터로 액세스 토큰을 성공적으로 생성한다")
        void shouldGenerateAccessTokenWithValidParameters() {
            // given
            Long userId = 1L;
            final String provider = "KAKAO";
            String role = "USER";

            given(comcodeService.getCodeValue("ACCESS")).willReturn("ACCESS");

            // when
            String accessToken = jwtContext.generateAccessToken(userId, provider, role);

            // then
            assertThat(accessToken).isNotNull();
            assertThat(accessToken).isNotEmpty();
        }

        @Test
        @DisplayName("유효한 파라미터로 리프레시 토큰을 성공적으로 생성한다")
        void shouldGenerateRefreshTokenWithValidParameters() {
            // given
            Long userId = 1L;

            given(comcodeService.getCodeValue("REFRESH")).willReturn("REFRESH");

            given(comcodeService.getCodeValue("REFRESH")).willReturn("REFRESH");

            // when
            String refreshToken = jwtContext.generateRefreshToken(userId, "KAKAO");

            // then
            assertThat(refreshToken).isNotNull();
            assertThat(refreshToken).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("HTTP 요청에서 토큰 추출 테스트")
    class ExtractTokenFromRequestTest {

        @Test
        @DisplayName("유효한 Bearer 토큰을 성공적으로 추출한다")
        void shouldExtractValidBearerToken() {
            // given
            String expectedToken = "valid-token";
            given(request.getHeader("Authorization")).willReturn("Bearer " + expectedToken);

            // when
            String extractedToken = jwtContext.extractTokenFromRequest(request);

            // then
            assertThat(extractedToken).isEqualTo(expectedToken);
        }

        @Test
        @DisplayName("Authorization 헤더가 없을 때 예외를 던진다")
        void shouldThrowExceptionWhenAuthorizationHeaderMissing() {
            // given
            given(request.getHeader("Authorization")).willReturn(null);

            // when & then
            assertThatThrownBy(() -> jwtContext.extractTokenFromRequest(request))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("Bearer 토큰 형식이 아닐 때 예외를 던진다")
        void shouldThrowExceptionWhenNotBearerFormat() {
            // given
            given(request.getHeader("Authorization")).willReturn("Basic invalid-token");

            // when & then
            assertThatThrownBy(() -> jwtContext.extractTokenFromRequest(request))
                .isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    @DisplayName("토큰에서 사용자 ID 추출 테스트")
    class ExtractSubjectTest {

        @Test
        @DisplayName("유효한 토큰에서 사용자 ID를 성공적으로 추출한다")
        void shouldExtractSubjectFromValidToken() {
            // given
            Long expectedUserId = 123L;
            given(comcodeService.getCodeValue("ACCESS")).willReturn("ACCESS");
            String token = jwtContext.generateAccessToken(expectedUserId, "KAKAO", "USER");

            // when
            Long extractedUserId = jwtContext.extractSubject(token);

            // then
            assertThat(extractedUserId).isEqualTo(expectedUserId);
        }

        @Test
        @DisplayName("null 토큰일 때 예외를 던진다")
        void shouldThrowExceptionWhenTokenIsNull() {
            // when & then
            assertThatThrownBy(() -> jwtContext.extractSubject(null))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("잘못된 형식의 토큰일 때 예외를 던진다")
        void shouldThrowExceptionWhenTokenIsInvalid() {
            // given
            String invalidToken = "invalid.token.format";

            // when & then
            assertThatThrownBy(() -> jwtContext.extractSubject(invalidToken))
                .isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    @DisplayName("토큰에서 토큰 타입 추출 테스트")
    class ExtractTokenTypeTest {

        @Test
        @DisplayName("유효한 액세스 토큰에서 토큰 타입을 성공적으로 추출한다")
        void shouldExtractTokenTypeFromValidAccessToken() {
            // given
            given(comcodeService.getCodeValue("ACCESS")).willReturn("ACCESS");
            String token = jwtContext.generateAccessToken(1L, "KAKAO", "USER");

            // when
            String tokenType = jwtContext.extractTokenType(token);

            // then
            assertThat(tokenType).isEqualTo("ACCESS");
        }
    }

    @Nested
    @DisplayName("토큰 만료 시간 조회 테스트")
    class GetExpirationTest {

        @Test
        @DisplayName("액세스 토큰 만료 시간을 성공적으로 조회한다")
        void shouldGetAccessTokenExpiration() {
            // when
            Long expiration = jwtContext.getAccessTokenExpiration();

            // then
            assertThat(expiration).isEqualTo(ACCESS_TOKEN_EXPIRATION);
        }
    }
} 