package com.climbx.climbx.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;

import com.climbx.climbx.auth.dto.AccessTokenResponseDto;
import com.climbx.climbx.auth.dto.CallbackRequestDto;
import com.climbx.climbx.auth.dto.TokenGenerationResponseDto;
import com.climbx.climbx.auth.dto.ValidatedTokenInfoDto;
import com.climbx.climbx.auth.entity.UserAuthEntity;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.provider.ProviderIdTokenService;
import com.climbx.climbx.auth.repository.UserAuthRepository;
import com.climbx.climbx.common.dto.JwtTokenInfoDto;
import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.enums.TokenType;
import com.climbx.climbx.common.exception.InvalidTokenException;
import com.climbx.climbx.common.util.JwtContext;
import com.climbx.climbx.fixture.UserFixture;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.video.repository.VideoRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {


    @Mock
    private JwtContext jwtContext;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private UserStatRepository userStatRepository;

    @Mock
    private ProviderIdTokenService providerIdTokenService;

    @Mock
    private NonceService nonceService;

    @Mock
    private RefreshTokenBlacklistService refreshTokenBlacklistService;

    @Mock
    private UserRankingHistoryRepository userRankingHistoryRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("OAuth2 콜백 처리 테스트")
    class HandleCallbackTest {

        @Test
        @DisplayName("기존 사용자에 대해 콜백을 성공적으로 처리한다")
        void shouldHandleCallbackForExistingUser() {
            // given
            UserAccountEntity user = UserAccountEntity.builder()
                .userId(1L)
                .nickname("테스트유저")
                .role(RoleType.USER)
                .build();
            UserAuthEntity userAuth = UserAuthEntity.builder()
                .userAccountEntity(user)
                .provider(OAuth2ProviderType.KAKAO)
                .providerId("12345")
                .providerEmail("test@example.com")
                .isPrimary(true)
                .build();

            AccessTokenResponseDto accessTokenResponse = AccessTokenResponseDto.builder()
                .accessToken("access-token-1")
                .expiresIn(3600L)
                .build();

            given(userAuthRepository.findByProviderAndProviderId(
                OAuth2ProviderType.KAKAO, "12345")
            ).willReturn(Optional.of(userAuth));
            given(providerIdTokenService.verifyIdToken(OAuth2ProviderType.KAKAO, "valid-id-token",
                "test-nonce")).willReturn(
                ValidatedTokenInfoDto.builder()
                    .providerId("12345")
                    .email("test@example.com")
                    .nickname("테스트유저")
                    .build()
            );
            given(jwtContext.generateAccessToken(1L, RoleType.USER)).willReturn(
                accessTokenResponse);
            given(jwtContext.generateRefreshToken(1L)).willReturn("refresh-token-1");

            CallbackRequestDto request = CallbackRequestDto.builder()
                .idToken("valid-id-token")
                .nonce("test-nonce")
                .build();

            // when
            TokenGenerationResponseDto result = authService.handleCallback(OAuth2ProviderType.KAKAO,
                request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken().accessToken()).isEqualTo("access-token-1");
            assertThat(result.accessToken().expiresIn()).isEqualTo(3600L);
            assertThat(result.refreshToken()).isEqualTo("refresh-token-1");

            then(providerIdTokenService).should()
                .verifyIdToken(OAuth2ProviderType.KAKAO, "valid-id-token", "test-nonce");
        }

        @Test
        @DisplayName("존재하지 않는 사용자인 경우 새 사용자를 생성한다")
        void shouldCreateNewUserWhenUserNotFound() {
            // given
            given(userAuthRepository.findByProviderAndProviderId(
                OAuth2ProviderType.KAKAO, "67890")
            ).willReturn(Optional.empty());
            given(userAccountRepository.findByNickname(anyString())).willReturn(Optional.empty());
            given(userAccountRepository.save(any())).willAnswer(invocation -> {
                UserAccountEntity entity = invocation.getArgument(0);
                return UserAccountEntity.builder()
                    .userId(2L)
                    .nickname(entity.nickname())
                    .role(entity.role())
                    .build();
            });
            given(providerIdTokenService.verifyIdToken(OAuth2ProviderType.KAKAO, "valid-id-token",
                "test-nonce")).willReturn(
                ValidatedTokenInfoDto.builder()
                    .providerId("67890")
                    .email("newuser@example.com")
                    .nickname("새유저")
                    .build()
            );

            AccessTokenResponseDto accessTokenResponse = AccessTokenResponseDto.builder()
                .accessToken("access-token-2")
                .expiresIn(3600L)
                .build();

            given(jwtContext.generateAccessToken(2L, RoleType.USER)).willReturn(
                accessTokenResponse);
            given(jwtContext.generateRefreshToken(2L)).willReturn("refresh-token-2");

            CallbackRequestDto request = CallbackRequestDto.builder()
                .idToken("valid-id-token")
                .nonce("test-nonce")
                .build();

            // when
            TokenGenerationResponseDto result = authService.handleCallback(OAuth2ProviderType.KAKAO,
                request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken().accessToken()).isEqualTo("access-token-2");
            assertThat(result.accessToken().expiresIn()).isEqualTo(3600L);
            assertThat(result.refreshToken()).isEqualTo("refresh-token-2");

            then(providerIdTokenService).should()
                .verifyIdToken(OAuth2ProviderType.KAKAO, "valid-id-token", "test-nonce");
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 갱신한다")
        void shouldRefreshAccessTokenWithValidRefreshToken() {
            // given
            doNothing().when(refreshTokenBlacklistService)
                .validateTokenNotBlacklisted("valid-refresh-token");
            JwtTokenInfoDto tokenInfo = JwtTokenInfoDto.builder()
                .userId(3L)
                .role("USER")
                .tokenType(TokenType.REFRESH)
                .build();
            given(jwtContext.parseToken("valid-refresh-token")).willReturn(tokenInfo);

            UserAccountEntity user = UserAccountEntity.builder()
                .userId(3L)
                .nickname("리프레시유저")
                .role(RoleType.USER)
                .build();
            given(userAccountRepository.findById(3L)).willReturn(Optional.of(user));
            doNothing().when(refreshTokenBlacklistService).addToBlacklist("valid-refresh-token");

            AccessTokenResponseDto accessTokenResponse = AccessTokenResponseDto.builder()
                .accessToken("new-access-token")
                .expiresIn(3600L)
                .build();

            given(jwtContext.generateAccessToken(3L, RoleType.USER)).willReturn(
                accessTokenResponse);
            given(jwtContext.generateRefreshToken(3L)).willReturn("new-refresh-token");

            // when
            TokenGenerationResponseDto result = authService.refreshAccessToken(
                "valid-refresh-token");

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken().accessToken()).isEqualTo("new-access-token");
            assertThat(result.accessToken().expiresIn()).isEqualTo(3600L);
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");

            then(refreshTokenBlacklistService).should()
                .validateTokenNotBlacklisted("valid-refresh-token");
            then(refreshTokenBlacklistService).should().addToBlacklist("valid-refresh-token");
        }

        @Test
        @DisplayName("잘못된 토큰 타입일 때 예외를 던진다")
        void shouldThrowExceptionWhenTokenTypeIsNotRefresh() {
            // given
            doNothing().when(refreshTokenBlacklistService)
                .validateTokenNotBlacklisted("access-token");

            JwtTokenInfoDto tokenInfo = JwtTokenInfoDto.builder()
                .userId(1L)
                .role("USER")
                .tokenType(TokenType.ACCESS)
                .build();

            given(jwtContext.parseToken("access-token")).willReturn(tokenInfo);

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken("access-token"))
                .isInstanceOf(InvalidTokenException.class);

            then(userAccountRepository).should(never()).findById(anyLong());
            then(jwtContext).should(never()).generateAccessToken(anyLong(), any());
            then(refreshTokenBlacklistService).should(never()).addToBlacklist(anyString());
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외를 던진다")
        void shouldThrowExceptionWhenUserNotFoundInRefresh() {
            // given
            doNothing().when(refreshTokenBlacklistService)
                .validateTokenNotBlacklisted("valid-refresh-token");

            JwtTokenInfoDto tokenInfo = JwtTokenInfoDto.builder()
                .userId(999L)
                .role(null)
                .tokenType(TokenType.REFRESH)
                .build();

            given(jwtContext.parseToken("valid-refresh-token")).willReturn(tokenInfo);
            given(userAccountRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken("valid-refresh-token"))
                .isInstanceOf(InvalidTokenException.class);

            then(jwtContext).should(never()).generateAccessToken(anyLong(), any());
            then(refreshTokenBlacklistService).should(never()).addToBlacklist(anyString());
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트")
    class UnregisterUserTest {

        @Test
        @DisplayName("유효한 사용자 ID로 회원 탈퇴를 성공적으로 처리한다")
        void shouldUnregisterUserSuccessfully() {
            // given
            Long userId = 1L;
            String refreshToken = "valid-refresh-token";

            UserAccountEntity userAccount = UserFixture.createUser(userId);

            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.of(userAccount));
            given(userStatRepository.softDeleteByUserId(userId)).willReturn(1);
            given(userAuthRepository.softDeleteAllByUserId(userId)).willReturn(2);
            given(videoRepository.softDeleteAllByUserId(userId)).willReturn(5);
            given(submissionRepository.softDeleteAllByUserId(userId)).willReturn(3);
            given(userRankingHistoryRepository.softDeleteAllByUserId(userId)).willReturn(10);

            doNothing().when(refreshTokenBlacklistService).addToBlacklist(refreshToken);

            // when
            authService.unregisterUser(userId, refreshToken);

            // then
            // 1. 로그아웃 처리 검증
            then(refreshTokenBlacklistService).should().addToBlacklist(refreshToken);

            // 2. 사용자 조회 검증
            then(userAccountRepository).should().findByUserId(userId);

            // 3. 모든 관련 리소스 bulk delete 검증
            then(userStatRepository).should().softDeleteByUserId(userId);
            then(userAuthRepository).should().softDeleteAllByUserId(userId);
            then(videoRepository).should().softDeleteAllByUserId(userId);
            then(submissionRepository).should().softDeleteAllByUserId(userId);
            then(userRankingHistoryRepository).should().softDeleteAllByUserId(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 회원 탈퇴 시도 시 예외를 던진다")
        void shouldThrowExceptionWhenUserNotFoundForUnregister() {
            // given
            Long nonExistentUserId = 999L;
            String refreshToken = "valid-refresh-token";

            given(userAccountRepository.findByUserId(nonExistentUserId)).willReturn(
                Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.unregisterUser(nonExistentUserId, refreshToken))
                .isInstanceOf(UserNotFoundException.class);

            // 로그아웃은 처리되었는지 확인
            then(refreshTokenBlacklistService).should(never()).addToBlacklist(anyString());
            then(userAccountRepository).should().findByUserId(nonExistentUserId);

            // 사용자가 존재하지 않으므로 bulk delete 메소드들은 호출되지 않음
            then(userStatRepository).should(never()).softDeleteByUserId(anyLong());
            then(userAuthRepository).should(never()).softDeleteAllByUserId(anyLong());
            then(videoRepository).should(never()).softDeleteAllByUserId(anyLong());
            then(submissionRepository).should(never()).softDeleteAllByUserId(anyLong());
            then(userRankingHistoryRepository).should(never()).softDeleteAllByUserId(anyLong());
        }

        @Test
        @DisplayName("사용자는 존재하지만 관련 데이터가 없는 경우에도 성공적으로 처리한다")
        void shouldUnregisterUserSuccessfullyEvenWithNoRelatedData() {
            // given
            Long userId = 2L;
            String refreshToken = "valid-refresh-token";

            UserAccountEntity userAccount = UserFixture.createUser(userId);

            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.of(userAccount));
            given(userStatRepository.softDeleteByUserId(userId)).willReturn(0);
            given(userAuthRepository.softDeleteAllByUserId(userId)).willReturn(0);
            given(videoRepository.softDeleteAllByUserId(userId)).willReturn(0);
            given(submissionRepository.softDeleteAllByUserId(userId)).willReturn(0);
            given(userRankingHistoryRepository.softDeleteAllByUserId(userId)).willReturn(0);

            doNothing().when(refreshTokenBlacklistService).addToBlacklist(refreshToken);

            // when
            authService.unregisterUser(userId, refreshToken);

            // then
            // 1. 로그아웃 처리 검증
            then(refreshTokenBlacklistService).should().addToBlacklist(refreshToken);

            // 2. 사용자 조회 검증
            then(userAccountRepository).should().findByUserId(userId);

            // 3. 모든 관련 리소스 bulk delete 검증 (데이터가 없어도 bulk delete는 수행됨)
            then(userStatRepository).should().softDeleteByUserId(userId);
            then(userAuthRepository).should().softDeleteAllByUserId(userId);
            then(videoRepository).should().softDeleteAllByUserId(userId);
            then(submissionRepository).should().softDeleteAllByUserId(userId);
            then(userRankingHistoryRepository).should().softDeleteAllByUserId(userId);
        }
    }
}