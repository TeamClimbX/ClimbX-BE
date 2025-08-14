package com.climbx.climbx.user;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.dto.UserProfileInfoModifyRequestDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
@Tag(name = "User", description = "사용자 관련 API")
public interface UserApiDocumentation {

    @Operation(
        summary = "사용자 목록 조회",
        description = "검색 키워드를 사용하여 사용자 목록을 조회합니다. 키워드가 없으면 모든 사용자를 반환합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자 목록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/users",
                          "data": [
                            {
                              "id": 1,
                              "nickname": "클라이머123",
                              "email": "user1@example.com",
                              "name": "홍길동",
                              "profileImageUrl": null,
                              "createdAt": "2024-01-01T10:00:00Z"
                            },
                            {
                              "id": 2,
                              "nickname": "클라이머456",
                              "email": "user2@example.com",
                              "name": "김철수",
                              "profileImageUrl": null,
                              "createdAt": "2024-01-02T10:00:00Z"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 검색 파라미터입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/users",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<UserProfileResponseDto> getUsers(
        @Parameter(
            name = "search",
            description = "검색 키워드 (닉네임, 이름 등)",
            required = false,
            example = "클라이머"
        )
        String search
    );

    @Operation(
        summary = "사용자 프로필 조회",
        description = "닉네임을 사용하여 특정 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 프로필 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자 프로필",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/users/클라이머123",
                          "data": {
                            "id": 1,
                            "nickname": "클라이머123",
                            "email": "user@example.com",
                            "name": "홍길동",
                            "profileImageUrl": null,
                            "createdAt": "2024-01-01T10:00:00Z",
                            "statistics": {
                              "totalProblems": 156,
                              "totalAttempts": 342,
                              "averageGrade": "V4",
                              "streakDays": 14
                            }
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 닉네임",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 닉네임",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 닉네임 형식입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 34,
                          "path": "/api/users/invalid-nickname",
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
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "해당 닉네임의 사용자를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 78,
                          "path": "/api/users/nonexistent",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    UserProfileResponseDto getUserByNickname(
        @Parameter(
            name = "nickname",
            description = "사용자 닉네임",
            required = true,
            example = "클라이머123"
        )
        @NotBlank
        String nickname
    );

    @Operation(
        summary = "사용자 프로필 수정",
        description = "현재 로그인한 사용자의 프로필 정보를 수정합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "프로필 수정 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "프로필 수정 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-15T10:00:00Z",
                          "responseTimeMs": 134,
                          "path": "/api/users/클라이머123",
                          "data": {
                            "id": 1,
                            "nickname": "새로운닉네임",
                            "email": "user@example.com",
                            "name": "홍길동",
                            "profileImageUrl": "https://example.com/profile.jpg",
                            "createdAt": "2024-01-01T10:00:00Z",
                            "updatedAt": "2024-01-15T10:00:00Z"
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 유효하지 않은 데이터",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 프로필 데이터입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/users/클라이머123",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                          "path": "/api/users/클라이머123",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "권한 없음 (다른 사용자의 프로필)",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                        {
                          "httpStatus": 403,
                          "statusMessage": "다른 사용자의 프로필을 수정할 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/users/다른사용자",
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
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "해당 사용자를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/users/nonexistent",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "중복된 닉네임",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "중복된 닉네임",
                    value = """
                        {
                          "httpStatus": 409,
                          "statusMessage": "이미 사용 중인 닉네임입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 134,
                          "path": "/api/users/클라이머123",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    UserProfileResponseDto modifyUserProfileInfo(
        @Parameter(hidden = true)
        Long userId,
        @Parameter(
            name = "nickname",
            description = "현재 사용자 닉네임",
            required = true,
            example = "클라이머123"
        )
        @NotBlank
        String nickname,
        @Parameter(
            description = "사용자 프로필 수정 요청 데이터"
        )
        @Valid
        UserProfileInfoModifyRequestDto modifyRequest
    );

    @Operation(
        summary = "사용자 프로필 이미지 수정",
        description = "현재 로그인한 사용자의 프로필 이미지를 수정합니다. 이미지 파일을 업로드하여 S3에 저장하고 CDN URL을 반환합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "프로필 이미지 수정 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "프로필 이미지 수정 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-15T10:00:00Z",
                          "responseTimeMs": 234,
                          "path": "/api/users/클라이머123/profile-image",
                          "data": {
                            "nickname": "클라이머123",
                            "statusMessage": "열심히 등반 중!",
                            "profileImageUrl": "https://cdn.climbx.com/profile-images/123_1642248000000.jpg",
                            "ranking": 15,
                            "rating": 1500,
                            "categoryRatings": {},
                            "currentStreak": 7,
                            "longestStreak": 21,
                            "solvedProblemsCount": 156,
                            "rivalCount": 8
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 유효하지 않은 이미지 파일",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 이미지 파일",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "이미지 파일만 업로드 가능합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/users/클라이머123/profile-image",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                          "path": "/api/users/클라이머123/profile-image",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "권한 없음 (다른 사용자의 프로필)",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                        {
                          "httpStatus": 403,
                          "statusMessage": "다른 사용자의 프로필을 수정할 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/users/다른사용자/profile-image",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413",
            description = "파일 크기 초과",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "파일 크기 초과",
                    value = """
                        {
                          "httpStatus": 413,
                          "statusMessage": "이미지 파일 크기는 5MB 이하여야 합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 134,
                          "path": "/api/users/클라이머123/profile-image",
                          "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 오류 (S3 업로드 실패 등)",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "httpStatus": 500,
                          "statusMessage": "이미지 업로드에 실패했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 567,
                          "path": "/api/users/클라이머123/profile-image",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    UserProfileResponseDto updateUserProfileImage(
        @Parameter(hidden = true)
        Long userId,
        @Parameter(
            name = "nickname",
            description = "현재 사용자 닉네임",
            required = true,
            example = "클라이머123"
        )
        @NotBlank
        String nickname,
        @Parameter(
            name = "profileImage",
            description = "업로드할 프로필 이미지 파일 (JPG, PNG, GIF 지원, 최대 5MB)",
            required = false,
            content = @Content(mediaType = "multipart/form-data")
        )
        MultipartFile profileImage
    );

    @Operation(
        summary = "사용자 최고 난이도 문제 조회",
        description = "특정 사용자가 완등한 최고 난이도 문제들을 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "최고 난이도 문제 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "최고 난이도 문제 목록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 189,
                          "path": "/api/users/클라이머123/top-problems",
                          "data": [
                            {
                              "id": 1,
                              "name": "Red Wall Challenge",
                              "grade": "V8",
                              "gymId": 1,
                              "gymName": "클라이밍 파크",
                              "completedAt": "2024-01-10T15:30:00Z",
                              "attempts": 15
                            },
                            {
                              "id": 2,
                              "name": "Overhang Master",
                              "grade": "V7",
                              "gymId": 2,
                              "gymName": "더 클라임",
                              "completedAt": "2024-01-08T14:20:00Z",
                              "attempts": 22
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 limit 값입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/users/클라이머123/top-problems",
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
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "해당 사용자를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/users/nonexistent/top-problems",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<ProblemInfoResponseDto> getUserTopProblems(
        @Parameter(
            name = "nickname",
            description = "사용자 닉네임",
            required = true,
            example = "클라이머123"
        )
        @NotBlank
        String nickname,
        @Parameter(
            name = "limit",
            description = "조회할 문제 수 (1-20)",
            required = false,
            example = "20"
        )
        @Min(1)
        @Max(20)
        Integer limit
    );

    @Operation(
        summary = "사용자 연속 등반 기록 조회",
        description = "특정 사용자의 연속 등반 기록을 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "연속 등반 기록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "연속 등반 기록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 167,
                          "path": "/api/users/클라이머123/streak",
                          "data": [
                            {
                              "date": "2024-01-15",
                              "problemsCompleted": 5,
                              "totalAttempts": 12,
                              "averageGrade": "V5",
                              "timeSpent": 180
                            },
                            {
                              "date": "2024-01-14",
                              "problemsCompleted": 3,
                              "totalAttempts": 8,
                              "averageGrade": "V4",
                              "timeSpent": 120
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 날짜 형식입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 56,
                          "path": "/api/users/클라이머123/streak",
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
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "해당 사용자를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 78,
                          "path": "/api/users/nonexistent/streak",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<DailyHistoryResponseDto> getUserStreak(
        @Parameter(
            name = "nickname",
            description = "사용자 닉네임",
            required = true,
            example = "클라이머123"
        )
        @NotBlank
        String nickname,
        @Parameter(
            name = "from",
            description = "조회 시작 날짜 (YYYY-MM-DD)",
            required = false,
            example = "2023-01-01"
        )
        LocalDate from,
        @Parameter(
            name = "to",
            description = "조회 종료 날짜 (YYYY-MM-DD)",
            required = false,
            example = "2023-12-31"
        )
        LocalDate to
    );

    @Operation(
        summary = "사용자 일별 기록 조회",
        description = "특정 사용자의 일별 등반 기록을 조건에 따라 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "일별 기록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "일별 기록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 198,
                          "path": "/api/users/클라이머123/history",
                          "data": [
                            {
                              "date": "2024-01-15",
                              "problemsCompleted": 8,
                              "totalAttempts": 15,
                              "averageGrade": "V6",
                              "timeSpent": 240,
                              "gymVisited": "클라이밍 파크"
                            },
                            {
                              "date": "2024-01-14",
                              "problemsCompleted": 6,
                              "totalAttempts": 12,
                              "averageGrade": "V5",
                              "timeSpent": 180,
                              "gymVisited": "더 클라임"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 조회 조건입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/users/클라이머123/history",
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
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "사용자 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "해당 사용자를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/users/nonexistent/history",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<DailyHistoryResponseDto> getUserDailyHistory(
        @Parameter(
            name = "nickname",
            description = "사용자 닉네임",
            required = true,
            example = "클라이머123"
        )
        @NotBlank
        String nickname,
        @Parameter(
            name = "criteria",
            description = "조회 기준",
            required = true,
            schema = @Schema(implementation = String.class)
        )
        @NotNull
        CriteriaType criteria,
        @Parameter(
            name = "from",
            description = "조회 시작 날짜 (YYYY-MM-DD)",
            required = false,
            example = "2023-01-01"
        )
        LocalDate from,
        @Parameter(
            name = "to",
            description = "조회 종료 날짜 (YYYY-MM-DD)",
            required = false,
            example = "2023-12-31"
        )
        LocalDate to
    );
}

