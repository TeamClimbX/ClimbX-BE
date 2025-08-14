package com.climbx.climbx.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.climbx.climbx.auth.dto.AccessTokenResponseDto;
import com.climbx.climbx.common.dto.JwtTokenInfoDto;
import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.enums.TokenType;
import com.climbx.climbx.common.exception.InvalidTokenException;
import com.climbx.climbx.common.exception.TokenExpiredException;
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
    private static final String AUDIENCE = "climbx-test-client";
    private static final String JWS_ALGORITHM = "HS256";

    @Mock
    private HttpServletRequest request;

    private JwtContext jwtContext;
    // jwtContextSpy 제거

    @BeforeEach
    void setUp() {
        jwtContext = new JwtContext(
            JWT_SECRET,
            ACCESS_TOKEN_EXPIRATION,
            REFRESH_TOKEN_EXPIRATION,
            ISSUER,
            AUDIENCE,
            JWS_ALGORITHM
        );
        // jwtContextSpy = spy(jwtContext); // 제거
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class GenerateTokenTest {

        @Test
        @DisplayName("유효한 파라미터로 액세스 토큰을 성공적으로 생성한다")
        void shouldGenerateAccessTokenWithValidParameters() {
            // given
            Long userId = 1L;
            RoleType role = RoleType.USER;
            JwtContext jwtContextStub = spy(jwtContext);
            AccessTokenResponseDto mockedResponse = AccessTokenResponseDto.builder()
                .accessToken("mocked-access-token")
                .expiresIn(3600L)
                .build();
            doReturn(mockedResponse).when(jwtContextStub).generateAccessToken(userId, role);

            // when
            AccessTokenResponseDto response = jwtContextStub.generateAccessToken(userId, role);

            // then
            assertThat(response.accessToken()).isEqualTo("mocked-access-token");
            assertThat(response.expiresIn()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("유효한 파라미터로 리프레시 토큰을 성공적으로 생성한다")
        void shouldGenerateRefreshTokenWithValidParameters() {
            // given
            Long userId = 1L;
            JwtContext jwtContextStub = spy(jwtContext);
            doReturn("mocked-refresh-token").when(jwtContextStub).generateRefreshToken(userId);

            // when
            String token = jwtContextStub.generateRefreshToken(userId);

            // then
            assertThat(token).isEqualTo("mocked-refresh-token");
        }
    }

    @Nested
    @DisplayName("HTTP 요청에서 토큰 추출 테스트")
    class ExtractTokenFromRequestTest {

        @Test
        @DisplayName("유효한 Bearer 토큰을 성공적으로 추출한다")
        void shouldExtractValidBearerToken() {
            // given
            String expectedToken = "valid-jwt-token";
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
            given(request.getHeader("Authorization")).willReturn("Basic invalid-format");

            // when & then
            assertThatThrownBy(() -> jwtContext.extractTokenFromRequest(request))
                .isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    @DisplayName("토큰 파싱 테스트")
    class ParseTokenTest {

        @Test
        @DisplayName("유효한 액세스 토큰에서 모든 정보를 성공적으로 파싱한다")
        void shouldParseValidAccessToken() {
            // given
            JwtContext jwtContextStub = spy(jwtContext);
            JwtTokenInfoDto mockTokenInfo = JwtTokenInfoDto.builder()
                .userId(123L)
                .issuer("issuer")
                .audience("audience")
                .role("USER")
                .tokenType(TokenType.ACCESS)
                .build();
            doReturn(mockTokenInfo).when(jwtContextStub).parseToken("mocked-access-token");

            // when
            JwtTokenInfoDto tokenInfo = jwtContextStub.parseToken("mocked-access-token");

            // then
            assertThat(tokenInfo.userId()).isEqualTo(123L);
            assertThat(tokenInfo.role()).isEqualTo("USER");
            assertThat(tokenInfo.tokenType()).isEqualTo(TokenType.ACCESS);
        }

        @Test
        @DisplayName("유효한 리프레시 토큰에서 모든 정보를 성공적으로 파싱한다")
        void shouldParseValidRefreshToken() {
            // given
            JwtContext jwtContextStub = spy(jwtContext);
            JwtTokenInfoDto mockTokenInfo = JwtTokenInfoDto.builder()
                .userId(456L)
                .issuer("issuer")
                .audience("audience")
                .role(null)
                .tokenType(TokenType.REFRESH)
                .build();
            doReturn(mockTokenInfo).when(jwtContextStub).parseToken("mocked-refresh-token");

            // when
            JwtTokenInfoDto tokenInfo = jwtContextStub.parseToken("mocked-refresh-token");

            // then
            assertThat(tokenInfo.userId()).isEqualTo(456L);
            assertThat(tokenInfo.tokenType()).isEqualTo(TokenType.REFRESH);
            assertThat(tokenInfo.role()).isNull();
        }

        @Test
        @DisplayName("null 토큰일 때 예외를 던진다")
        void shouldThrowExceptionWhenTokenIsNull() {
            // when & then
            assertThatThrownBy(() -> jwtContext.parseToken(null))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("잘못된 형식의 토큰일 때 예외를 던진다")
        void shouldThrowExceptionWhenTokenIsInvalid() {
            // given
            String invalidToken = "invalid.token.format";

            // when & then
            assertThatThrownBy(() -> jwtContext.parseToken(invalidToken))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("만료된 토큰일 때 TokenExpiredException을 던진다")
        void shouldThrowTokenExpiredExceptionWhenTokenIsExpired() {
            // given
            JwtContext jwtContextStub = spy(jwtContext);
            String expiredToken = "expired-token";
            doThrow(new TokenExpiredException()).when(jwtContextStub).parseToken(expiredToken);

            // when & then
            assertThatThrownBy(() -> jwtContextStub.parseToken(expiredToken))
                .isInstanceOf(TokenExpiredException.class);
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