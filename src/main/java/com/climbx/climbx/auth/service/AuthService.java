package com.climbx.climbx.auth.service;

import com.climbx.climbx.auth.dto.AccessTokenResponseDto;
import com.climbx.climbx.auth.dto.CallbackRequestDto;
import com.climbx.climbx.auth.dto.TokenGenerationResponseDto;
import com.climbx.climbx.auth.dto.UserAuthResponseDto;
import com.climbx.climbx.auth.dto.ValidatedTokenInfoDto;
import com.climbx.climbx.auth.entity.UserAuthEntity;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.exception.UserAuthNotFoundException;
import com.climbx.climbx.auth.provider.ProviderIdTokenService;
import com.climbx.climbx.auth.provider.exception.ProviderNotSupportedException;
import com.climbx.climbx.auth.repository.UserAuthRepository;
import com.climbx.climbx.common.dto.JwtTokenInfoDto;
import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.enums.TokenType;
import com.climbx.climbx.common.exception.InvalidTokenException;
import com.climbx.climbx.common.util.JwtContext;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.video.repository.VideoRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final JwtContext jwtContext;
    private final UserAccountRepository userAccountRepository;
    private final UserAuthRepository userAuthsRepository;
    private final UserStatRepository userStatRepository;
    private final ProviderIdTokenService oauth2IdTokenService;
    private final RefreshTokenBlacklistService refreshTokenBlacklistService;
    private final UserRankingHistoryRepository userRankingHistoryRepository;
    private final VideoRepository videoRepository;
    private final SubmissionRepository submissionRepository;

    /**
     * OAuth2 콜백
     */
    @Transactional
    public TokenGenerationResponseDto handleCallback(String provider, CallbackRequestDto request) {
        // Provider 타입 검증
        OAuth2ProviderType providerType;
        try {
            providerType = OAuth2ProviderType.valueOf(provider.toUpperCase());
        } catch (Exception e) {
            throw new ProviderNotSupportedException(provider);
        }

        // ID Token 검증 및 사용자 정보 추출
        ValidatedTokenInfoDto tokenInfo = oauth2IdTokenService.verifyIdToken(
            provider,
            request.idToken(),
            request.nonce()
        );

        log.info("OAuth2 idToken 검증 성공: provider={}, providerId={}, email={}",
            provider, tokenInfo.providerId(), tokenInfo.email());

        // 사용자 정보로 계정 생성 또는 업데이트
        UserAccountEntity user = createOrUpdateUser(tokenInfo, providerType);

        // JWT 토큰 생성
        AccessTokenResponseDto accessToken = jwtContext.generateAccessToken(
            user.userId(),
            user.role()
        );

        String refreshToken = jwtContext.generateRefreshToken(user.userId());

        log.info("사용자 로그인 완료: userId={}, nickname={}, provider={}",
            user.userId(), user.nickname(), providerType.name());

        return TokenGenerationResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰을 발급합니다.
     */
    @Transactional
    public TokenGenerationResponseDto refreshAccessToken(String refreshToken) {
        try {
            // 1. 블랙리스트 확인
            refreshTokenBlacklistService.validateTokenNotBlacklisted(refreshToken);

            // 2. 토큰에서 모든 정보를 한 번에 파싱 및 검증
            JwtTokenInfoDto tokenInfo = jwtContext.parseToken(refreshToken);

            // 3. REFRESH 토큰인지 확인
            String refreshTokenType = TokenType.REFRESH.name();
            if (!refreshTokenType.equals(tokenInfo.tokenType().toUpperCase())) {
                log.debug("Invalid token type: expected={}, actual={}", refreshTokenType,
                    tokenInfo.tokenType());
                throw new InvalidTokenException();
            }

            // 4. 사용자 존재 확인
            UserAccountEntity user = userAccountRepository.findById(tokenInfo.userId())
                .orElseThrow(() -> new UserNotFoundException(tokenInfo.userId()));

            // 5. 기존 토큰을 블랙리스트에 추가 (로테이션)
            refreshTokenBlacklistService.addToBlacklist(refreshToken);

            // 6. 새로운 액세스 토큰 생성
            AccessTokenResponseDto newAccessToken = jwtContext.generateAccessToken(
                tokenInfo.userId(),
                user.role()
            );

            // 7. 새로운 리프레시 토큰 생성
            String newRefreshToken = jwtContext.generateRefreshToken(tokenInfo.userId());

            log.info("토큰 갱신 완료: userId={}", tokenInfo.userId());

            return TokenGenerationResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        } catch (Exception e) {
            log.error("리프레시 토큰 갱신 실패", e);
            throw new InvalidTokenException("리프레시 토큰 갱신에 실패했습니다.");
        }
    }

    /**
     * 현재 사용자 정보를 조회합니다.
     */
    public UserAuthResponseDto getCurrentUserInfo(Long userId) {
        // 사용자 주 인증 수단 조회
        UserAuthEntity userAuth = userAuthsRepository.findByUserIdAndIsPrimaryTrue(userId)
            .orElseThrow(() -> new UserAuthNotFoundException(userId));

        return UserAuthResponseDto.from(userAuth);
    }

    /**
     * 사용자 로그아웃을 처리합니다.
     */
    public void signOut(String refreshToken) {
        // 리프레시 토큰을 블랙리스트에 추가
        refreshTokenBlacklistService.addToBlacklist(refreshToken);
        log.info("사용자 로그아웃 완료");
    }

    /**
     * 회원 탈퇴를 처리합니다.
     */
    @Transactional
    public void unregisterUser(Long userId, String refreshToken) {
        // 2. 사용자 조회
        UserAccountEntity userAccount = userAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 1. 먼저 로그아웃 처리
        signOut(refreshToken);

        log.info("회원 탈퇴 처리 시작: userId={}, nickname={}", userId, userAccount.nickname());

        // 3. 사용자 소유 리소스들 bulk soft delete 처리 (성능 최적화)
        int deletedUserStats = userStatRepository.softDeleteByUserId(userId);
        int deletedUserAuths = userAuthsRepository.softDeleteAllByUserId(userId);
        int deletedVideos = videoRepository.softDeleteAllByUserId(userId);
        int deletedSubmissions = submissionRepository.softDeleteAllByUserId(userId);
        int deletedHistories = userRankingHistoryRepository.softDeleteAllByUserId(userId);

        // 4. 마지막에 사용자 계정 soft delete
        userAccount.softDelete();

        log.info(
            "회원 탈퇴 처리 완료: userId={}, deletedStats={}, deletedAuths={}, deletedVideos={}, deletedSubmissions={}, deletedHistories={}",
            userId, deletedUserStats, deletedUserAuths, deletedVideos, deletedSubmissions,
            deletedHistories);
    }

    /**
     * 사용자 정보로 계정을 생성하거나 업데이트합니다.
     */
    private UserAccountEntity createOrUpdateUser(
        ValidatedTokenInfoDto tokenInfo,
        OAuth2ProviderType providerType
    ) {
        // 기존 인증 정보 확인
        Optional<UserAuthEntity> existingUserAuth = userAuthsRepository
            .findByProviderAndProviderId(providerType, tokenInfo.providerId());

        if (existingUserAuth.isPresent()) {
            // 기존 사용자 정보 업데이트
            UserAccountEntity user = existingUserAuth.get().userAccountEntity();
            log.info("기존 사용자 로그인: userId={}, providerId={}", user.userId(), tokenInfo.providerId());
            return user;
        } else {
            // 새로운 사용자 생성
            return createNewUser(tokenInfo, providerType);
        }
    }

    /**
     * 새로운 사용자를 생성합니다.
     */
    private UserAccountEntity createNewUser(
        ValidatedTokenInfoDto tokenInfo,
        OAuth2ProviderType providerType
    ) {

        // 임시 닉네임 생성 (중복 방지)
        String providerNickname = tokenInfo.nickname();
        String nickname = userAccountRepository
            .findByNickname(providerNickname)
            .map(user -> generateTemporaryNickname(providerNickname))
            .orElse(providerNickname);

        // 사용자 계정 생성
        UserAccountEntity userAccount = UserAccountEntity.builder()
            .nickname(nickname)
            .role(RoleType.USER)
            .profileImageUrl(tokenInfo.profileImageUrl())
            .build();

        UserAccountEntity savedUser = userAccountRepository.save(userAccount);

        // 사용자 인증 정보 생성
        UserAuthEntity userAuth = UserAuthEntity.builder()
            .userAccountEntity(savedUser)
            .provider(providerType)
            .providerId(tokenInfo.providerId())
            .providerEmail(tokenInfo.email())
            .isPrimary(true)
            .build();

        userAuthsRepository.save(userAuth);

        // 사용자 통계 정보 초기화
        UserStatEntity userStat = UserStatEntity.builder()
            .userAccountEntity(savedUser)
            .build();

        userStatRepository.save(userStat);

        log.info("새로운 사용자 생성 완료: userId={}, nickname={}, providerId={}",
            savedUser.userId(), nickname, tokenInfo.providerId());

        return savedUser;
    }

    /**
     * 임시 닉네임을 생성합니다.
     */
    private String generateTemporaryNickname(String providerNickname) {
        return providerNickname + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}