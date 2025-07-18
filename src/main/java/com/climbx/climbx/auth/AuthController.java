package com.climbx.climbx.auth;

import com.climbx.climbx.auth.dto.AccessTokenResponseDto;
import com.climbx.climbx.auth.dto.CallbackRequestDto;
import com.climbx.climbx.auth.dto.TokenGenerationResponseDto;
import com.climbx.climbx.auth.dto.UserAuthResponseDto;
import com.climbx.climbx.common.annotation.SuccessStatus;
import com.climbx.climbx.common.security.JwtContext;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDocumentation {

    private final AuthService authService;
    private final JwtContext jwtContext;

    /**
     * provider의 인가 code를 받아 인증하고 토큰 발급
     */
    @Override
    @PostMapping("/oauth2/{provider}/callback")
    @SuccessStatus(value = HttpStatus.CREATED)
    public AccessTokenResponseDto handleCallback(
        @PathVariable String provider,
        @RequestBody CallbackRequestDto request,
        HttpServletResponse response
    ) {
        log.info(
            "{} OAuth2 콜백 처리 시작",
            provider.toUpperCase()
        );

        TokenGenerationResponseDto tokenResponse = authService.handleCallback(provider, request);

        // 리프레시 토큰을 HTTP Only Cookie로 설정
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(
            tokenResponse.refreshToken(),
            jwtContext.getRefreshTokenExpiration()
        );
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        log.info("{} OAuth2 콜백 처리 완료", provider.toUpperCase());

        return tokenResponse.accessToken();
    }

    /**
     * 액세스 토큰을 갱신합니다. 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
     */
    @Override
    @PostMapping("/oauth2/refresh")
    @SuccessStatus(value = HttpStatus.CREATED)
    public AccessTokenResponseDto refreshAccessToken(
        @CookieValue(value = "refreshToken", required = true) String refreshToken,
        HttpServletResponse response
    ) {
        log.info("액세스 토큰 갱신 요청");

        TokenGenerationResponseDto refreshResponse = authService.refreshAccessToken(refreshToken);

        // 새로운 리프레시 토큰을 HTTP Only Cookie로 설정
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(
            refreshResponse.refreshToken(),
            jwtContext.getRefreshTokenExpiration()
        );
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

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
        @CookieValue(value = "refreshToken", required = true) String refreshToken,
        HttpServletResponse response
    ) {
        log.info("로그아웃 요청");

        authService.signOut(refreshToken);

        // 리프레시 토큰 쿠키 삭제
        ResponseCookie refreshTokenCookie = clearRefreshTokenCookie();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        log.info("로그아웃 완료");
    }

    /**
     * 리프레시 토큰 쿠키를 생성합니다.
     */
    private ResponseCookie createRefreshTokenCookie(String value, long expiresIn) {
        return ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(false) // TODO: 프로덕션 환경에서는 true로 변경
            .path("/api/auth/oauth2/refresh")
            .maxAge(Duration.ofSeconds(expiresIn))
            .sameSite("Strict")
            .build();
    }

    /**
     * 리프레시 토큰 쿠키를 삭제합니다.
     */
    private ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false) // TODO: 프로덕션 환경에서는 true로 변경
            .path("/api/auth/signout")
            .maxAge(Duration.ZERO)
            .sameSite("Strict")
            .build();
    }
}