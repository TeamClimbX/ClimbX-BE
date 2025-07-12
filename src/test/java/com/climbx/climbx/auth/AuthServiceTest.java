package com.climbx.climbx.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.climbx.climbx.auth.dto.LoginResponseDto;
import com.climbx.climbx.auth.dto.OAuth2TokenResponseDto;
import com.climbx.climbx.auth.dto.OAuth2UserInfoDto;
import com.climbx.climbx.auth.dto.UserOauth2InfoResponseDto;
import com.climbx.climbx.auth.entity.UserAuthEntity;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.exception.InvalidRefreshTokenException;
import com.climbx.climbx.auth.exception.UserAuthNotFoundException;
import com.climbx.climbx.auth.provider.OAuth2Provider;
import com.climbx.climbx.auth.provider.OAuth2ProviderFactory;
import com.climbx.climbx.auth.repository.UserAuthRepository;
import com.climbx.climbx.common.comcode.ComcodeService;
import com.climbx.climbx.common.security.JwtContext;
import com.climbx.climbx.fixture.UserAuthFixture;
import com.climbx.climbx.fixture.UserFixture;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
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
    private ComcodeService comcodeService;

    @Mock
    private JwtContext jwtContext;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private UserStatRepository userStatRepository;

    @Mock
    private OAuth2ProviderFactory providerFactory;

    @Mock
    private OAuth2Provider oauth2Provider;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("OAuth2 콜백 처리 테스트")
    class HandleCallbackTest {

        @Test
        @DisplayName("새로운 사용자에 대해 콜백을 성공적으로 처리한다")
        void shouldHandleCallbackForNewUser() {
            // given
            final String provider = "kakao";
            String code = "test-code";
            String providerId = "12345";
            String email = "test@example.com";

            OAuth2TokenResponseDto tokenResponse = OAuth2TokenResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

            OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.builder()
                .providerId(providerId)
                .email(email)
                .nickname("테스트유저")
                .profileImageUrl("https://example.com/profile.jpg")
                .emailVerified(true)
                .build();

            // JWT 토큰 스터빙 - any() 매처 사용
            given(jwtContext.generateAccessToken(any(), any(), any())).willReturn(
                "jwt-access-token");
            given(jwtContext.generateRefreshToken(any(), any())).willReturn("jwt-refresh-token");
            given(jwtContext.getAccessTokenExpiration()).willReturn(3600L);

            // OAuth2 provider 스터빙 - String으로 변경
            given(providerFactory.getProvider("kakao")).willReturn(oauth2Provider);
            given(oauth2Provider.getProviderType()).willReturn(OAuth2ProviderType.KAKAO);
            given(oauth2Provider.exchangeCodeForToken(code)).willReturn(tokenResponse);
            given(oauth2Provider.fetchUserInfo("access-token")).willReturn(userInfo);

            // 기존 사용자 인증 정보 없음
            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.empty());

            // 기존 사용자 계정 없음
            given(userAccountRepository.findByEmail(email)).willReturn(Optional.empty());

            // 저장된 엔티티 반환
            UserAccountEntity savedUser = UserFixture.createUser(email, "클라이머_123");
            given(userAccountRepository.save(any(UserAccountEntity.class))).willReturn(savedUser);

            // when
            given(userAuthRepository.save(any(UserAuthEntity.class))).willReturn(
                mock(UserAuthEntity.class));
            given(userStatRepository.save(any(UserStatEntity.class))).willReturn(
                mock(UserStatEntity.class));

            // when
            LoginResponseDto result = authService.handleCallback(provider, code);

            // then
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.accessToken()).isEqualTo("jwt-access-token");
            assertThat(result.refreshToken()).isEqualTo("jwt-refresh-token");
            assertThat(result.expiresIn()).isEqualTo(3600L);

            then(userAccountRepository).should().save(any(UserAccountEntity.class));
            then(userAuthRepository).should().save(any(UserAuthEntity.class));
            then(userStatRepository).should().save(any(UserStatEntity.class));
        }

        @Test
        @DisplayName("기존 사용자에 대해 콜백을 성공적으로 처리한다")
        void shouldHandleCallbackForExistingUser() {
            // given
            final String provider = "kakao";
            String code = "test-code";
            String providerId = "12345";

            OAuth2TokenResponseDto tokenResponse = OAuth2TokenResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

            OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.builder()
                .providerId(providerId)
                .email("test@example.com")
                .nickname("기존유저")
                .emailVerified(true)
                .build();

            UserAccountEntity existingUser = UserFixture.createUser();
            UserAuthEntity existingAuth = UserAuthFixture.createKakaoAuth(existingUser, providerId);

            given(jwtContext.generateAccessToken(any(), any(), any())).willReturn(
                "jwt-access-token");
            given(jwtContext.generateRefreshToken(any(), any())).willReturn("jwt-refresh-token");
            given(jwtContext.getAccessTokenExpiration()).willReturn(3600L);

            // OAuth2 provider 스터빙 - String으로 변경
            given(providerFactory.getProvider("kakao")).willReturn(oauth2Provider);
            given(oauth2Provider.getProviderType()).willReturn(OAuth2ProviderType.KAKAO);
            given(oauth2Provider.exchangeCodeForToken(code)).willReturn(tokenResponse);
            given(oauth2Provider.fetchUserInfo("access-token")).willReturn(userInfo);

            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.of(existingAuth));

            // when
            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.of(existingAuth));

            // when
            LoginResponseDto result = authService.handleCallback(provider, code);

            // then
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.accessToken()).isEqualTo("jwt-access-token");

            then(userAccountRepository).should(never()).save(any(UserAccountEntity.class));
        }

        @Test
        @DisplayName("기존 사용자에게 새 OAuth2 제공자를 연결한다")
        void shouldLinkNewOAuth2ProviderToExistingUser() {
            // given
            final String provider = "kakao";
            String code = "test-code";
            String providerId = "new-provider-id";
            String email = "test@example.com";

            OAuth2TokenResponseDto tokenResponse = OAuth2TokenResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

            OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.builder()
                .providerId(providerId)
                .email(email)
                .nickname("기존유저")
                .emailVerified(true)
                .build();

            UserAccountEntity existingUser = UserFixture.createUser(email, "기존유저");

            given(jwtContext.generateAccessToken(any(), any(), any())).willReturn(
                "jwt-access-token");
            given(jwtContext.generateRefreshToken(any(), any())).willReturn("jwt-refresh-token");
            given(jwtContext.getAccessTokenExpiration()).willReturn(3600L);

            // OAuth2 provider 스터빙 - String으로 변경
            given(providerFactory.getProvider("kakao")).willReturn(oauth2Provider);
            given(oauth2Provider.getProviderType()).willReturn(OAuth2ProviderType.KAKAO);
            given(oauth2Provider.exchangeCodeForToken(code)).willReturn(tokenResponse);
            given(oauth2Provider.fetchUserInfo("access-token")).willReturn(userInfo);

            // 새 provider ID로는 기존 인증 정보 없음
            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.empty());

            // 이메일로 기존 사용자 찾기
            given(userAccountRepository.findByEmail(email)).willReturn(Optional.of(existingUser));

            // 아직 연결되지 않은 제공자
            given(userAuthRepository.existsByUserAccountEntity_UserIdAndProvider(1L,
                OAuth2ProviderType.KAKAO))
                .willReturn(false);
            given(userAuthRepository.save(any(UserAuthEntity.class))).willReturn(
                mock(UserAuthEntity.class));

            // when
            LoginResponseDto result = authService.handleCallback(provider, code);

            // then
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.accessToken()).isEqualTo("jwt-access-token");

            then(userAuthRepository).should().save(any(UserAuthEntity.class));
            then(userAccountRepository).should(never()).save(any(UserAccountEntity.class));
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshAccessTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 갱신한다")
        void shouldRefreshAccessTokenWithValidRefreshToken() {
            // given
            String refreshToken = "valid-refresh-token";
            Long userId = 1L;
            final String provider = "KAKAO";

            UserAccountEntity user = UserFixture.createUser();

            given(comcodeService.getCodeValue("REFRESH")).willReturn("REFRESH");
            given(jwtContext.extractTokenType(refreshToken)).willReturn("REFRESH");
            given(jwtContext.extractSubject(refreshToken)).willReturn(userId);
            given(jwtContext.extractProvider(refreshToken)).willReturn(provider);
            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(jwtContext.generateAccessToken(userId, provider, user.role())).willReturn(
                "new-access-token");
            given(jwtContext.generateRefreshToken(userId, provider)).willReturn(
                "new-refresh-token");
            given(jwtContext.getAccessTokenExpiration()).willReturn(3600L);

            // when
            LoginResponseDto result = authService.refreshAccessToken(refreshToken);

            // then
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(result.expiresIn()).isEqualTo(3600L);

            then(jwtContext).should().extractTokenType(refreshToken);
            then(jwtContext).should().extractSubject(refreshToken);
            then(jwtContext).should().extractProvider(refreshToken);
            then(jwtContext).should().generateAccessToken(userId, provider, user.role());
            then(jwtContext).should().generateRefreshToken(userId, provider);
        }

        @Test
        @DisplayName("토큰 타입이 refresh가 아닐 때 예외를 던진다")
        void shouldThrowExceptionWhenTokenTypeIsNotRefresh() {
            // given
            String refreshToken = "invalid-type-token";

            given(comcodeService.getCodeValue("REFRESH")).willReturn("REFRESH");
            given(jwtContext.extractTokenType(refreshToken)).willReturn("ACCESS");

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("유효하지 않은 리프레시 토큰입니다.");

            then(jwtContext).should().extractTokenType(refreshToken);
        }

        @Test
        @DisplayName("토큰 타입을 확인할 수 없을 때 예외를 던진다")
        void shouldThrowExceptionWhenTokenTypeCannotBeExtracted() {
            // given
            String refreshToken = "invalid-token";

            given(jwtContext.extractTokenType(refreshToken))
                .willThrow(new RuntimeException("Invalid token"));

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(RuntimeException.class);

            then(jwtContext).should().extractTokenType(refreshToken);
        }

        @Test
        @DisplayName("토큰에서 사용자 정보를 추출할 수 없을 때 예외를 던진다")
        void shouldThrowExceptionWhenUserIdCannotBeExtracted() {
            // given
            String refreshToken = "invalid-token";

            given(comcodeService.getCodeValue("REFRESH")).willReturn("REFRESH");
            given(jwtContext.extractTokenType(refreshToken)).willReturn("REFRESH");
            given(jwtContext.extractSubject(refreshToken))
                .willThrow(new RuntimeException("Invalid token"));

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(RuntimeException.class);

            then(jwtContext).should().extractTokenType(refreshToken);
            then(jwtContext).should().extractSubject(refreshToken);
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외를 던진다")
        void shouldThrowExceptionWhenUserNotFound() {
            // given
            String refreshToken = "valid-refresh-token";
            Long userId = 999L;

            given(comcodeService.getCodeValue("REFRESH")).willReturn("REFRESH");
            given(jwtContext.extractTokenType(refreshToken)).willReturn("REFRESH");
            given(jwtContext.extractSubject(refreshToken)).willReturn(userId);
            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

            then(jwtContext).should().extractTokenType(refreshToken);
            then(jwtContext).should().extractSubject(refreshToken);
        }
    }

    @Nested
    @DisplayName("현재 사용자 정보 조회 테스트")
    class GetCurrentUserInfoTest {

        @Test
        @DisplayName("현재 사용자 정보를 성공적으로 조회한다")
        void shouldGetCurrentUserInfoSuccessfully() {
            // given
            Long userId = 1L;
            UserAccountEntity user = UserFixture.createUser();
            UserAuthEntity primaryAuth = UserAuthFixture.createKakaoAuth(user);

            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(userAuthRepository.findByUserIdAndIsPrimaryTrue(userId)).willReturn(
                Optional.of(primaryAuth));
            given(jwtContext.getAccessTokenExpiration()).willReturn(3600L);

            // when
            UserOauth2InfoResponseDto result = authService.getCurrentUserInfo(userId);

            // then
            assertThat(result.id()).isEqualTo(user.userId());
            assertThat(result.nickname()).isEqualTo(user.nickname());
            assertThat(result.provider()).isEqualTo("KAKAO");
            assertThat(result.issuedAt()).isNotNull();
            assertThat(result.expiresAt()).isNotNull();

            then(userAccountRepository).should().findByUserId(userId);
            then(userAuthRepository).should().findByUserIdAndIsPrimaryTrue(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외를 던진다")
        void shouldThrowExceptionWhenUserNotFound() {
            // given
            Long userId = 999L;

            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUserInfo(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

            then(userAccountRepository).should().findByUserId(userId);
        }

        @Test
        @DisplayName("주 인증 수단이 없는 사용자 조회 시 예외를 던진다")
        void shouldThrowExceptionWhenNoPrimaryAuth() {
            // given
            Long userId = 1L;
            UserAccountEntity user = UserFixture.createUser();

            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(userAuthRepository.findByUserIdAndIsPrimaryTrue(userId)).willReturn(
                Optional.empty());

            // when
            given(userAuthRepository.findByUserIdAndIsPrimaryTrue(userId)).willReturn(
                Optional.empty());

            // when
            assertThatThrownBy(() -> authService.getCurrentUserInfo(userId))
                .isInstanceOf(UserAuthNotFoundException.class)
                .hasMessage("사용자 인증 정보를 찾을 수 없습니다.");

            // then
            then(userAccountRepository).should().findByUserId(userId);
            then(userAuthRepository).should().findByUserIdAndIsPrimaryTrue(userId);
        }
    }

    @Nested
    @DisplayName("사용자 생성/업데이트 테스트")
    class CreateOrUpdateUserTest {

        @Test
        @DisplayName("검증되지 않은 이메일로는 계정 연결을 하지 않는다")
        void shouldNotLinkAccountWithUnverifiedEmail() {
            // given
            final String provider = "kakao";
            String code = "test-code";
            String providerId = "12345";
            String email = "test@example.com";

            OAuth2TokenResponseDto tokenResponse = OAuth2TokenResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

            OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.builder()
                .providerId(providerId)
                .email(email)
                .nickname("테스트유저")
                .emailVerified(false) // 검증되지 않은 이메일
                .build();

            // JWT 토큰 스터빙 - any() 매처 사용
            given(jwtContext.generateAccessToken(any(), any(), any())).willReturn(
                "jwt-access-token");
            given(jwtContext.generateRefreshToken(any(), any())).willReturn("jwt-refresh-token");
            given(jwtContext.getAccessTokenExpiration()).willReturn(3600L);

            // OAuth2 provider 스터빙 - String으로 변경
            given(providerFactory.getProvider("kakao")).willReturn(oauth2Provider);
            given(oauth2Provider.getProviderType()).willReturn(OAuth2ProviderType.KAKAO);
            given(oauth2Provider.exchangeCodeForToken(code)).willReturn(tokenResponse);
            given(oauth2Provider.fetchUserInfo("access-token")).willReturn(userInfo);

            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.empty());

            UserAccountEntity savedUser = UserFixture.createUser(email, "클라이머_123");

            // when
            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.empty());

            given(userAccountRepository.save(any(UserAccountEntity.class))).willReturn(savedUser);
            given(userAuthRepository.save(any(UserAuthEntity.class))).willReturn(
                mock(UserAuthEntity.class));
            given(userStatRepository.save(any(UserStatEntity.class))).willReturn(
                mock(UserStatEntity.class));

            // when
            authService.handleCallback(provider, code);

            // then
            // 기존 사용자 이메일 조회를 하지 않았는지 확인
            then(userAccountRepository).should(never()).findByEmail(email);
            // 새 사용자를 생성했는지 확인
            then(userAccountRepository).should().save(any(UserAccountEntity.class));
        }

        @Test
        @DisplayName("이미 연결된 제공자로 로그인 시 추가 저장 없이 로그인된다")
        void shouldLoginWithoutAdditionalSaveWhenProviderAlreadyLinked() {
            // given
            final String provider = "kakao";
            String code = "test-code";
            String providerId = "new-provider-id";
            String email = "test@example.com";

            OAuth2TokenResponseDto tokenResponse = OAuth2TokenResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

            OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.builder()
                .providerId(providerId)
                .email(email)
                .nickname("기존유저")
                .emailVerified(true)
                .build();

            UserAccountEntity existingUser = UserFixture.createUser(email, "기존유저");

            given(jwtContext.generateAccessToken(any(), any(), any())).willReturn(
                "jwt-access-token");
            given(jwtContext.generateRefreshToken(any(), any())).willReturn("jwt-refresh-token");
            given(jwtContext.getAccessTokenExpiration()).willReturn(3600L);

            // OAuth2 provider 스터빙 - String으로 변경
            given(providerFactory.getProvider("kakao")).willReturn(oauth2Provider);
            given(oauth2Provider.getProviderType()).willReturn(OAuth2ProviderType.KAKAO);
            given(oauth2Provider.exchangeCodeForToken(code)).willReturn(tokenResponse);
            given(oauth2Provider.fetchUserInfo("access-token")).willReturn(userInfo);

            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.empty());
            given(userAccountRepository.findByEmail(email)).willReturn(Optional.of(existingUser));

            // 이미 연결된 제공자
            given(userAuthRepository.existsByUserAccountEntity_UserIdAndProvider(1L,
                OAuth2ProviderType.KAKAO))
                .willReturn(true);

            // when
            given(userAuthRepository.findByProviderAndProviderId(OAuth2ProviderType.KAKAO,
                providerId))
                .willReturn(Optional.empty());
            given(userAccountRepository.findByEmail(email)).willReturn(Optional.of(existingUser));

            // 이미 연결된 제공자
            given(userAuthRepository.existsByUserAccountEntity_UserIdAndProvider(1L,
                OAuth2ProviderType.KAKAO))
                .willReturn(true);

            // when
            LoginResponseDto result = authService.handleCallback(provider, code);

            // then
            assertThat(result.accessToken()).isEqualTo("jwt-access-token");

            // 새로운 인증 정보 저장이 없었는지 확인
            then(userAuthRepository).should(never()).save(any(UserAuthEntity.class));
            then(userAccountRepository).should(never()).save(any(UserAccountEntity.class));
        }
    }
}