package com.climbx.climbx.problem;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.problem.dto.ContributionRequestDto;
import com.climbx.climbx.gym.enums.GymTierType;
import com.climbx.climbx.problem.enums.HoldColorType;
import com.climbx.climbx.problem.dto.ContributionResponseDto;
import com.climbx.climbx.problem.dto.ProblemCreateRequestDto;
import com.climbx.climbx.problem.dto.ProblemCreateResponseDto;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.enums.ProblemTierType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/**
 * 문제 관련 API 문서화를 위한 인터페이스
 * <p>
 * 이 인터페이스는 클라이밍 문제와 관련된 API 엔드포인트들을 정의합니다. Swagger UI에서 "Problem" 태그로 그룹화됩니다.
 */
@Validated
@Tag(name = "Problem", description = "클라이밍 문제 관련 API")
public interface ProblemApiDocumentation {

    @Operation(
        summary = "문제 목록 조회",
        description = "클라이밍장 ID, 영역 ID, 난이도 색상, 홀드 색상, 티어, 활성 상태 조건으로 문제 목록을 조회합니다. 조건에 맞는 문제들을 리스트로 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "문제 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "문제 목록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 145,
                          "path": "/api/problems",
                          "data": [
                            {
                              "problemId": "123e4567-e89b-12d3-a456-426614174000",
                              "gymId": 1,
                              "gymName": "테스트 클라이밍장",
                              "gymAreaId": 1,
                              "gymAreaName": "메인 월",
                              "localLevel": "V3",
                              "holdColor": "빨강",
                              "problemRating": 1500,
                              "problemTier": "P3",
                              "problemImageCdnUrl": "https://cdn.example.com/problem1.jpg",
                              "activeStatus": "ACTIVE"
                            },
                            {
                              "problemId": "987f6543-d21e-43c2-b765-321098765432",
                              "gymId": 1,
                              "gymName": "테스트 클라이밍장",
                              "gymAreaId": 1,
                              "gymAreaName": "메인 월",
                              "localLevel": "V3",
                              "holdColor": "빨강",
                              "problemRating": 1600,
                              "problemTier": "P2",
                              "problemImageCdnUrl": "https://cdn.example.com/problem2.jpg",
                              "activeStatus": "ACTIVE"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "httpStatus": 500,
                          "statusMessage": "서버 내부 오류가 발생했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/problems",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<ProblemInfoResponseDto> getProblemsWithFilters(
        @Parameter(
            name = "gymId",
            description = "클라이밍장 ID",
            required = false,
            example = "1"
        )
        @Min(1L)
        Long gymId,

        @Parameter(
            name = "gymAreaId",
            description = "클라이밍장 영역 ID",
            required = false,
            example = "1"
        )
        @Min(1L)
        Long gymAreaId,

        @Parameter(
            name = "localLevel",
            description = "문제 레벨 (클라이밍장별 난이도)",
            required = false,
            example = "V3"
        )
        GymTierType localLevel,

        @Parameter(
            name = "holdColor",
            description = "홀드 색상",
            required = false,
            example = "빨강"
        )
        HoldColorType holdColor,

        @Parameter(
            name = "problemTier",
            description = "문제 티어 (선택사항, 예: G3, P3, P2)",
            required = false,
            example = "P3"
        )
        ProblemTierType problemTier,

        @Parameter(
            name = "activeStatus",
            description = "문제 상태 (ACTIVE: 활성, INACTIVE: 비활성)",
            required = false,
            example = "ACTIVE"
        )
        ActiveStatusType activeStatus
    );

    @Operation(
        summary = "문제 생성",
        description = "새로운 클라이밍 문제를 생성합니다. 문제 정보와 선택적으로 이미지를 업로드할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "문제 생성 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "문제 생성 성공",
                    value = """
                        {
                          "httpStatus": 201,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 234,
                          "path": "/api/problems",
                          "data": {
                            "problemId": "123e4567-e89b-12d3-a456-426614174000",
                            "gymId": 1,
                            "gymName": "테스트 클라이밍장",
                            "gymAreaId": 1,
                            "areaName": "메인 월",
                            "localLevel": "V3",
                            "holdColor": "빨강",
                            "problemRating": 1500,
                            "problemImageCdnUrl": "https://cdn.example.com/problem-images/1_1640995200000.jpg",
                            "activeStatus": "ACTIVE"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 유효성 검사 실패",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "유효성 검사 실패",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "VALIDATION_FAILED",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/problems",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "클라이밍장 영역을 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "영역 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "GYM_AREA_NOT_FOUND",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/problems",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "httpStatus": 500,
                          "statusMessage": "서버 내부 오류가 발생했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/problems",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    ProblemCreateResponseDto registerProblem(
        @Parameter(
            description = "문제 생성 요청 데이터",
            required = true
        )
        @Valid
        ProblemCreateRequestDto request,

        @Parameter(
            description = "문제 이미지 파일 (선택사항, 최대 10MB)",
            required = true
        )
        @NotNull
        MultipartFile problemImage
    );

    @Operation(
        summary = "문제 투표",
        description = "클라이밍 문제에 대한 티어 및 태그를 투표합니다. 사용자가 문제의 난이도와 특성에 대해 의견을 제시할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "문제 투표 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "투표 성공",
                    value = """
                        {
                          "httpStatus": 201,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000/votes",
                          "data": {
                            "problemId": "123e4567-e89b-12d3-a456-426614174000",
                            "gymId": 1,
                            "gymName": "테스트 클라이밍장",
                            "gymAreaId": 1,
                            "gymAreaName": "메인 월",
                            "localLevel": "V3",
                            "holdColor": "빨강",
                            "problemRating": 1520,
                            "problemImageCdnUrl": "https://cdn.example.com/problem1.jpg",
                            "activeStatus": "ACTIVE"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 유효성 검사 실패",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "유효성 검사 실패",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "VALIDATION_FAILED",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000/votes",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "문제를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "문제 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "PROBLEM_NOT_FOUND",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 78,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000/votes",
                          "data": null
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
                    name = "인증 실패",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "UNAUTHORIZED",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 23,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000/votes",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    ProblemInfoResponseDto voteProblem(
        @Parameter(
            description = "투표하는 사용자 ID",
            required = true
        )
        Long userId,

        @Parameter(
            description = "투표할 문제 ID",
            required = true
        )
        UUID problemId,

        @Parameter(
            description = "문제 투표 요청 데이터 (티어, 댓글, 태그)",
            required = true
        )
        @Valid
        ContributionRequestDto voteRequest
    );

    @Operation(
        summary = "문제 투표 목록 조회",
        description = "특정 클라이밍 문제에 대한 투표 목록을 페이징으로 조회합니다. 다른 사용자들이 제출한 티어, 태그, 댓글 정보를 확인할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "문제 투표 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "투표 목록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 98,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000/votes",
                          "data": [
                            {
                              "nickname": "클라이머123",
                              "tier": "P3",
                              "tags": ["BALANCE", "CRIMPY"],
                              "comment": "발가락 힘이 중요한 문제입니다. 홀드가 작아서 핑거 스트렝스가 필요해요."
                            },
                            {
                              "nickname": "볼더링마스터",
                              "tier": "P2",
                              "tags": ["OVERHANG", "DYNAMIC"],
                              "comment": "다이나믹한 움직임이 핵심입니다. 충분한 웜업 후 시도하세요."
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "문제를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "문제 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "PROBLEM_NOT_FOUND",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000/votes",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "httpStatus": 500,
                          "statusMessage": "서버 내부 오류가 발생했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000/votes",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<ContributionResponseDto> getProblemVotes(
        @Parameter(
            description = "투표 목록을 조회할 문제 ID",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID problemId,

        @Parameter(
            description = "페이징 정보 (page: 페이지 번호, size: 페이지 크기)",
            example = "page=0&size=20"
        )
        Pageable pageable
    );

    @Operation(
        summary = "문제 삭제",
        description = "기존 클라이밍 문제를 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "문제 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "이미 삭제된 문제",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "이미 삭제된 문제",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "PROBLEM_ALREADY_DELETED",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "문제를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "문제 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "PROBLEM_NOT_FOUND",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/problems/123e4567-e89b-12d3-a456-426614174000",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    void softDeleteProblem(
        @Parameter(
            description = "문제 ID",
            required = true
        )
        UUID problemId
    );
}