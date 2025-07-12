package com.climbx.climbx.auth;

import com.climbx.climbx.auth.dto.LoginResponseDto;
import com.climbx.climbx.auth.dto.RefreshRequestDto;
import com.climbx.climbx.auth.dto.UserOauth2InfoResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

@Validated
@Tag(name = "Authentication", description = "인증 관련 API")
public interface AuthApiDocumentation {

    @Operation(
        summary = "Kakao OAuth2 인증 페이지로 리다이렉트 (개발용)",
        description = "Kakao OAuth2 인증 페이지로 자동 리다이렉트합니다. 개발 및 테스트 환경에서만 사용해야 합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "302",
            description = "카카오 인증 페이지로 리다이렉트",
            content = @Content(
                schema = @Schema(implementation = Void.class),
                examples = @ExampleObject(
                    name = "리다이렉트 응답",
                    value = """
                        HTTP 302 Found
                        Location: https://kauth.kakao.com/oauth/authorize?client_id=...&redirect_uri=...&response_type=code
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "httpStatus": 500,
                          "statusMessage": "서버 내부 오류가 발생했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 100,
                          "path": "/api/auth/oauth2/kakao/authorize-url",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<Void> redirectToKakaoAuthorize();

    @Operation(
        summary = "OAuth2 콜백 처리",
        description = "OAuth2 인증 완료 후 콜백을 처리하고 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "인증 성공",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/auth/oauth2/google/callback",
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTYiLCJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImlhdCI6MTcxNzY2ODAwMCwiZXhwIjoxNzE4MjczNjAwfQ.2mvWMa_AJeIk_2q1VCEB14IJKL5TrHUzO1yk30ByI9I",
                            "tokenType": "Bearer",
                            "expiresIn": 3600
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 인증 코드",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 인증 코드",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 인증 코드입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/auth/oauth2/google/callback",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "OAuth2 인증에 실패했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 234,
                          "path": "/api/auth/oauth2/google/callback",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    LoginResponseDto handleOAuth2Callback(
        @Parameter(
            name = "provider",
            description = "OAuth2 제공자",
            required = true,
            example = "google"
        )
        @NotBlank String provider,
        @Parameter(
            name = "code",
            description = "OAuth2 인증 코드",
            required = true
        )
        @NotBlank String code
    );

    @Operation(
        summary = "액세스 토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "토큰 갱신 성공",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "토큰 갱신 성공",
                    value = """
                        {
                          "httpStatus": 201,
                          "statusMessage": "토큰이 성공적으로 갱신되었습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 78,
                          "path": "/api/auth/oauth2/refresh",
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                            "tokenType": "Bearer",
                            "expiresIn": 3600
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 요청 형식입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 43,
                          "path": "/api/auth/oauth2/refresh",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 리프레시 토큰",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "유효하지 않은 토큰",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "유효하지 않은 리프레시 토큰입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/auth/oauth2/refresh",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    LoginResponseDto refreshAccessToken(
        @Parameter(
            description = "리프레시 토큰 요청",
            required = true
        )
        @jakarta.validation.Valid RefreshRequestDto request
    );

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 정보 조회 성공",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "사용자 정보",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 78,
                          "path": "/api/auth/me",
                          "data": {
                            "id": 1,
                            "email": "user@example.com",
                            "name": "홍길동",
                            "nickname": "클라이머123",
                            "provider": "google",
                            "role": "USER"
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증되지 않은 사용자",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "인증이 필요합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 23,
                          "path": "/api/auth/me",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "권한이 없는 사용자",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                        {
                          "httpStatus": 403,
                          "statusMessage": "접근 권한이 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 34,
                          "path": "/api/auth/me",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "사용자 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "사용자를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 56,
                          "path": "/api/auth/me",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    UserOauth2InfoResponseDto getCurrentUserInfo(
        @Parameter(hidden = true)
        Long userId
    );

    @Operation(
        summary = "로그아웃",
        description = "현재 로그인한 사용자를 로그아웃합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "로그아웃 성공",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "로그아웃 성공",
                    value = """
                        {
                          "httpStatus": 204,
                          "statusMessage": "로그아웃이 완료되었습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/auth/signout",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = com.climbx.climbx.common.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 요청 형식입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/auth/signout",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    void signOut(
        @Parameter(
            description = "리프레시 토큰 요청 (임시 API)",
            required = true
        )
        @jakarta.validation.Valid RefreshRequestDto request
    );
} 