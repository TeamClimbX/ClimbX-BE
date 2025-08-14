package com.climbx.climbx.auth;

import com.climbx.climbx.auth.dto.AccessTokenResponseDto;
import com.climbx.climbx.auth.dto.CallbackRequestDto;
import com.climbx.climbx.auth.dto.TokenGenerationResponseDto;
import com.climbx.climbx.auth.dto.UserAuthResponseDto;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.service.AuthService;
import com.climbx.climbx.common.annotation.SuccessStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDocumentation {

    private static final String REFRESH_TOKEN_HEADER = "Refresh-Token";
    private final AuthService authService;

    /**
     * provider의 인가 code를 받아 인증하고 토큰 발급
     */
    @Override
    @PostMapping("/oauth2/{providerType}/callback")
    @SuccessStatus(value = HttpStatus.CREATED)
    public AccessTokenResponseDto handleCallback(
        @PathVariable OAuth2ProviderType providerType,
        @RequestBody CallbackRequestDto request,
        HttpServletResponse response
    ) {
        log.info("{} OAuth2 콜백 처리 시작", providerType);

        TokenGenerationResponseDto tokenResponse = authService
            .handleCallback(providerType, request);

        response.setHeader(REFRESH_TOKEN_HEADER, tokenResponse.refreshToken());

        log.info("{} OAuth2 콜백 처리 완료", providerType);

        return tokenResponse.accessToken();
    }

    /**
     * 액세스 토큰을 갱신합니다. 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
     */
    @Override
    @PostMapping("/oauth2/refresh")
    @SuccessStatus(value = HttpStatus.CREATED)
    public AccessTokenResponseDto refreshAccessToken(
        @RequestHeader(REFRESH_TOKEN_HEADER) String refreshToken,
        HttpServletResponse response
    ) {
        log.info("액세스 토큰 갱신 요청");

        TokenGenerationResponseDto refreshResponse = authService.refreshAccessToken(refreshToken);

        response.setHeader(REFRESH_TOKEN_HEADER, refreshResponse.refreshToken());

        log.info("액세스 토큰 갱신 완료");
        return refreshResponse.accessToken();
    }

    /**
     * 현재 사용자의 SSO 관련 정보만 조회
     */
    @Override
    @GetMapping("/me")
    @SuccessStatus(value = HttpStatus.OK)
    public UserAuthResponseDto getCurrentUserInfo(
        @AuthenticationPrincipal Long userId
    ) {
        log.info("현재 사용자 정보 조회: userId={}", userId);

        UserAuthResponseDto response = authService.getCurrentUserInfo(userId);

        log.info("현재 사용자 정보 조회 완료: nickname={}", response.nickname());
        return response;
    }

    /**
     * 사용자 로그아웃을 처리합니다. 클라이언트에서 토큰을 삭제해주세요. 클라이언트에서도 토큰 삭제 필요
     */
    @Override
    @PostMapping("/signout")
    @SuccessStatus(value = HttpStatus.NO_CONTENT)
    public void signOut(
        @RequestHeader(REFRESH_TOKEN_HEADER) String refreshToken,
        HttpServletResponse response
    ) {
        log.info("로그아웃 요청");

        authService.signOut(refreshToken);

        log.info("로그아웃 완료");
    }

    /**
     * 회원 탈퇴를 처리합니다. 로그아웃 후 사용자 관련 데이터를 soft delete 처리합니다.
     */
    @Override
    @DeleteMapping("/unregister")
    @SuccessStatus(value = HttpStatus.NO_CONTENT)
    public void unregisterUser(
        @AuthenticationPrincipal Long userId,
        @RequestHeader(REFRESH_TOKEN_HEADER) String refreshToken,
        HttpServletResponse response
    ) {
        log.info("회원 탈퇴 요청: userId={}", userId);

        authService.unregisterUser(userId, refreshToken);

        log.info("회원 탈퇴 완료: userId={}", userId);
    }
}