package com.climbx.climbx.auth;

import com.climbx.climbx.auth.dto.AccessTokenResponseDto;
import com.climbx.climbx.auth.dto.CallbackRequestDto;
import com.climbx.climbx.auth.dto.UserAuthResponseDto;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;

@Validated
@Tag(name = "Authentication", description = "인증 관련 API")
public interface AuthApiDocumentation {

    @Operation(
        summary = "OAuth2 콜백 처리",
        description = "OAuth2 ID Token을 검증하고 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "인증 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "인증 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/auth/oauth2/{provider}/callback",
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                            "expiresIn": 3600
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 ID Token",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 ID Token",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 ID Token입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/auth/oauth2/kakao/callback",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "ID Token 검증에 실패했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 234,
                          "path": "/api/auth/oauth2/kakao/callback",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    AccessTokenResponseDto handleCallback(
        @Parameter(
            name = "providerType",
            description = "OAuth2 제공자",
            required = true,
            example = "kakao"
        )
        @NotNull OAuth2ProviderType providerType,
        @Parameter(
            name = "request",
            description = "ID Token과 Nonce를 포함한 콜백 요청",
            required = true,
            schema = @Schema(
                implementation = CallbackRequestDto.class,
                example = """
                    {
                      "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwicGljdHVyZSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vYXZhdGFyLmpwZyIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQyNjIyLCJub25jZSI6ImFiYzEyMyJ9.signature",
                      "nonce": "abc123"
                    }
                    """
            )
        )
        @Valid CallbackRequestDto request,
        @Parameter(hidden = true)
        HttpServletResponse response
    );

    @Operation(
        summary = "액세스 토큰 갱신",
        description = "HTTP Only 쿠키에 저장된 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "토큰 갱신 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
                            "expiresIn": 3600
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "리프레시 토큰이 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 43,
                          "path": "/api/auth/oauth2/refresh",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 리프레시 토큰",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
    AccessTokenResponseDto refreshAccessToken(
        @Parameter(
            name = "refreshToken",
            description = "HTTP Only 쿠키에 저장된 리프레시 토큰",
            required = true
        )
        @NotBlank String refreshToken,
        @Parameter(hidden = true)
        HttpServletResponse response
    );

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "사용자 정보 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
                            "nickname": "클라이머123",
                            "providerType": "KAKAO",
                            "providerId": "123456789",
                            "profileImageUrl": "https://example.com/profile.jpg"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
        @ApiResponse(
            responseCode = "403",
            description = "권한이 없는 사용자",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
    UserAuthResponseDto getCurrentUserInfo(
        @Parameter(hidden = true)
        Long userId
    );

    @Operation(
        summary = "로그아웃",
        description = "현재 로그인한 사용자를 로그아웃합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "로그아웃 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
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
        @RequestHeader(name = "Refresh-Token") String refreshToken,
        HttpServletResponse response
    );

    @Operation(
        summary = "회원 탈퇴",
        description = "현재 로그인한 사용자의 회원 탈퇴를 처리합니다. 로그아웃 후 사용자 관련 데이터를 soft delete 처리합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "회원 탈퇴 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "회원 탈퇴 성공",
                    value = """
                        {
                          "httpStatus": 204,
                          "statusMessage": "회원 탈퇴가 완료되었습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/auth/unregister",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 요청 형식입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/auth/unregister",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "인증이 필요합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/auth/unregister",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자를 찾을 수 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "사용자를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/auth/unregister",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    void unregisterUser(
        @AuthenticationPrincipal Long userId,
        @RequestHeader(name = "Refresh-Token") String refreshToken,
        HttpServletResponse response
    );
} 