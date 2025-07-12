package com.climbx.climbx.auth;

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
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final ComcodeService comcodeService;
    private final JwtContext jwtContext;
    private final UserAccountRepository userAccountRepository;
    private final UserAuthRepository userAuthsRepository;
    private final UserStatRepository userStatRepository;
    private final OAuth2ProviderFactory providerFactory;

    @Value("${spring.security.oauth2.kakao.client.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.kakao.client.redirect-uri}")
    private String kakaoRedirectUri;

    /**
     * 카카오 OAuth2 인증 URL 생성
     */
    public String generateKakaoAuthorizeUrl() {
        log.info("카카오 OAuth2 인증 URL 생성: client-id={}, redirect-uri={}",
            kakaoClientId, kakaoRedirectUri);

        return UriComponentsBuilder
            .fromUriString("https://kauth.kakao.com/oauth/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", kakaoClientId)
            .queryParam("redirect_uri", kakaoRedirectUri)
            .build()
            .toUriString();
    }

    /**
     * OAuth2 콜백
     */
    @Transactional
    public LoginResponseDto handleCallback(String provider, String code) {
        OAuth2Provider oauth2Provider = providerFactory.getProvider(provider);

        // 인가 코드로 액세스 토큰 교환
        OAuth2TokenResponseDto tokenResponse = oauth2Provider.exchangeCodeForToken(code);

        // 액세스 토큰으로 사용자 정보 조회
        OAuth2UserInfoDto userInfo = oauth2Provider.fetchUserInfo(tokenResponse.accessToken());

        // 사용자 정보로 계정 생성 또는 업데이트
        UserAccountEntity user = createOrUpdateUser(userInfo, oauth2Provider.getProviderType());

        // JWT 토큰 생성
        String accessToken = jwtContext.generateAccessToken(
            user.userId(),
            oauth2Provider.getProviderType().name(),
            user.role()
        );

        String refreshToken = jwtContext.generateRefreshToken(user.userId(),
            oauth2Provider.getProviderType().name());

        log.info("사용자 로그인 완료: userId={}, nickname={}, provider={}",
            user.userId(), user.nickname(), oauth2Provider.getProviderType().name());

        return LoginResponseDto.builder()
            .tokenType("Bearer")
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtContext.getAccessTokenExpiration())
            .build();
    }

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰을 발급합니다.
     */
    @Transactional
    public LoginResponseDto refreshAccessToken(String refreshToken) {
        Optional.of(jwtContext.extractTokenType(refreshToken))
            .filter(type -> type.equals(comcodeService.getCodeValue("REFRESH")))
            .orElseThrow(InvalidRefreshTokenException::new);

        Long userId = jwtContext.extractSubject(refreshToken);

        // 사용자 존재 확인
        UserAccountEntity user = userAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 기존 토큰에서 provider 정보 추출
        String provider = jwtContext.extractProvider(refreshToken);

        // 새로운 토큰 생성
        String newAccessToken = jwtContext.generateAccessToken(userId, provider, user.role());
        String newRefreshToken = jwtContext.generateRefreshToken(userId, provider);

        log.info("토큰 갱신 완료: userId={}", userId);

        return LoginResponseDto.builder()
            .tokenType("Bearer")
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .expiresIn(jwtContext.getAccessTokenExpiration())
            .build();
    }

    /**
     * 현재 사용자 SSO 정보 반환
     */
    public UserOauth2InfoResponseDto getCurrentUserInfo(Long userId) {
        UserAccountEntity user = userAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 사용자 주 인증 수단 조회
        String provider = userAuthsRepository.findByUserIdAndIsPrimaryTrue(userId)
            .map(userAuth -> userAuth.provider().name())
            .orElseThrow(() -> new UserAuthNotFoundException(userId));

        return UserOauth2InfoResponseDto.builder()
            .id(user.userId())
            .nickname(user.nickname())
            .provider(provider)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(jwtContext.getAccessTokenExpiration()))
            .build();
    }

    /**
     * 사용자 로그아웃을 처리합니다. 현재는 클라이언트에서 토큰 삭제로 처리됩니다.
     */
    public void signOut(String token) {
        // TODO: 추후 토큰 블랙리스트 기능 구현 시 추가
        log.info("사용자 로그아웃 요청");
    }

    /**
     * OAuth2 사용자 정보로 계정을 생성하거나 업데이트합니다. 이메일 기반으로 기존 사용자를 찾아 계정을 연결합니다.
     */
    private UserAccountEntity createOrUpdateUser(
        OAuth2UserInfoDto userInfo,
        OAuth2ProviderType providerType
    ) {
        String providerId = userInfo.providerId();
        String email = userInfo.email();

        // 기존 사용자 인증 정보 찾기
        Optional<UserAuthEntity> existingUserAuth
            = userAuthsRepository.findByProviderAndProviderId(providerType, providerId);

        if (existingUserAuth.isPresent()) {
            // 기존 사용자 로그인
            log.info(
                "기존 사용자 로그인: userId={}, email={}, provider={}",
                existingUserAuth.get().userAccountEntity().userId(),
                existingUserAuth.get().providerEmail(),
                providerType.name()
            );
            return existingUserAuth.get().userAccountEntity();
        }

        // 이메일로 기존 사용자 찾기 (이메일이 있고 검증된 경우)
        if (isValidEmailForLinking(email, userInfo.emailVerified())) {
            Optional<UserAccountEntity> linkedUser = userAccountRepository.findByEmail(email)
                .map(
                    existingUser -> linkNewOAuth2Provider(
                        existingUser,
                        userInfo,
                        providerType
                    )
                );

            if (linkedUser.isPresent()) {
                return linkedUser.get();
            }
        }

        // 사용자 생성
        return createNewUser(userInfo, providerType);
    }

    /**
     * 기존 사용자에게 새 OAuth2 제공자를 연결합니다.
     */
    private UserAccountEntity linkNewOAuth2Provider(
        UserAccountEntity existingUser,
        OAuth2UserInfoDto userInfo,
        OAuth2ProviderType providerType
    ) {

        String providerId = userInfo.providerId();

        // 이미 연결된 제공자인지 확인
        boolean alreadyLinked = userAuthsRepository.existsByUserAccountEntity_UserIdAndProvider(
            existingUser.userId(),
            providerType
        );

        if (!alreadyLinked) {
            // 새 provider 연결
            UserAuthEntity newAuth = UserAuthEntity.builder()
                .userAccountEntity(existingUser)
                .provider(providerType)
                .providerId(providerId)
                .providerEmail(userInfo.email())
                .isPrimary(false)
                .build();

            userAuthsRepository.save(newAuth);

            log.info(
                "기존 사용자에게 새 OAuth2 제공자 연결: userId={}, email={}, newProvider={}",
                existingUser.userId(),
                userInfo.email(),
                providerType.name()
            );
        } else {
            log.info(
                "이미 연결된 OAuth2 제공자로 로그인: userId={}, provider={}",
                existingUser.userId(),
                providerType.name())
            ;
        }

        return existingUser;
    }

    /**
     * 새로운 사용자를 생성합니다.
     */
    private UserAccountEntity createNewUser(OAuth2UserInfoDto userInfo,
        OAuth2ProviderType providerType) {

        String nickname = generateTemporaryNickname(userInfo.getDisplayName());

        // 1. 사용자 계정 생성
        UserAccountEntity newUser = UserAccountEntity.builder()
            .role(comcodeService.getCodeValue("USER"))
            .nickname(nickname)
            .email(userInfo.email())
            .profileImageUrl(userInfo.profileImageUrl())
            .statusMessage("안녕하세요!")
            .build();
        userAccountRepository.save(newUser);

        // 2. 사용자 인증 정보 생성 및 연결
        UserAuthEntity newUserAuth = UserAuthEntity.builder()
            .userAccountEntity(newUser)
            .provider(providerType)
            .providerId(userInfo.providerId())
            .providerEmail(userInfo.email())
            .isPrimary(true) // 첫 인증이므로 주 인증 수단으로 설정
            .build();
        userAuthsRepository.save(newUserAuth);

        // 3. 사용자 통계 정보 생성
        UserStatEntity newUserStat = UserStatEntity.builder()
            .userAccountEntity(newUser)
            .build();
        userStatRepository.save(newUserStat);

        log.info(
            "새로운 사용자 생성: userId={}, email={}, nickname={}, provider={}",
            newUser.userId(),
            newUser.email(),
            newUser.nickname(),
            providerType.name()
        );

        return newUser;
    }

    /**
     * 이메일이 계정 연결에 유효한지 확인합니다.
     */
    private boolean isValidEmailForLinking(String email, Boolean emailVerified) {
        // 이메일이 있고, 비어있지 않으며, 검증된 경우에만 연결 허용
        return email != null
            && !email.trim().isEmpty()
            && emailVerified != null
            && emailVerified;
    }

    /**
     * Unique한 초기 닉네임 생성
     */
    private String generateTemporaryNickname(String providerNickname) {
        return "클라이머_"
            + Optional.ofNullable(providerNickname).orElse("초보")
            + "_"
            + UUID.randomUUID().toString().substring(0, 8);
    }
}