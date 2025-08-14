package com.climbx.climbx.submission;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.submission.dto.SubmissionAppealRequestDto;
import com.climbx.climbx.submission.dto.SubmissionAppealResponseDto;
import com.climbx.climbx.submission.dto.SubmissionCancelResponseDto;
import com.climbx.climbx.submission.dto.SubmissionCreateRequestDto;
import com.climbx.climbx.submission.dto.SubmissionListResponseDto;
import com.climbx.climbx.submission.dto.SubmissionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
@Tag(name = "Submission", description = "문제 제출 관련 API")
public interface SubmissionApiDocumentation {

    @Operation(
        summary = "제출물 목록 조회",
        description = "다양한 필터 조건으로 제출물 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "제출물 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "제출물 목록 조회 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/submissions",
                          "data": {
                            "submissions": [
                              {
                                "id": 1,
                                "userId": 123,
                                "problemId": 456,
                                "videoId": "550e8400-e29b-41d4-a716-446655440000",
                                "rating": 5,
                                "holdColor": "RED",
                                "status": "APPROVED",
                                "submittedAt": "2024-01-01T09:00:00Z"
                              }
                            ],
                            "totalCount": 1,
                            "page": 0,
                            "perPage": 20
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청 파라미터",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효하지 않은 Enum 값입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/submissions",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    SubmissionListResponseDto getSubmissions(
        @Parameter(
            name = "nickname",
            description = "사용자 닉네임으로 필터링",
            example = "climbUser123"
        )
        String nickname,

        @Parameter(
            name = "problemId",
            description = "문제 ID로 필터링",
            example = "456"
        )
        Long problemId,

        @Parameter(
            name = "holdColor",
            description = "홀드 색상으로 필터링",
            example = "RED"
        )
        String holdColor,

        @Parameter(
            name = "ratingFrom",
            description = "최소 난이도 필터",
            example = "1"
        )
        Integer ratingFrom,

        @Parameter(
            name = "ratingTo",
            description = "최대 난이도 필터",
            example = "10"
        )
        Integer ratingTo,

        @Parameter(
            name = "pageable",
            description = "페이징 정보 (페이지 번호, 페이지 크기 등)",
            required = false
        )
        Pageable pageable
    );

    @Operation(
        summary = "제출물 생성",
        description = "새로운 문제 제출물을 생성합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "제출물 생성 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "제출물 생성 성공",
                    value = """
                        {
                          "httpStatus": 201,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 234,
                          "path": "/api/submissions",
                          "data": {
                            "id": 1,
                            "userId": 123,
                            "problemId": 456,
                            "videoId": "550e8400-e29b-41d4-a716-446655440000",
                            "rating": 5,
                            "holdColor": "RED",
                            "status": "PENDING",
                            "submittedAt": "2024-01-01T10:00:00Z"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청 데이터",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효성 검사에 실패했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/submissions",
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
                    name = "인증되지 않은 사용자",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "인증이 필요합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 23,
                          "path": "/api/submissions",
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
                    name = "문제를 찾을 수 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "문제를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/submissions",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    SubmissionResponseDto createSubmission(
        @Parameter(
            name = "request",
            description = "제출물 생성 요청 데이터",
            required = true,
            schema = @Schema(
                implementation = SubmissionCreateRequestDto.class,
                example = """
                    {
                      "problemId": 456,
                      "videoId": "550e8400-e29b-41d4-a716-446655440000",
                      "rating": 5,
                      "holdColor": "RED"
                    }
                    """
            )
        )
        Long userId,
        @Valid SubmissionCreateRequestDto request
    );

    @Operation(
        summary = "비디오 ID로 제출물 조회",
        description = "특정 비디오 ID에 해당하는 제출물을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "제출물 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "제출물 조회 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 78,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000",
                          "data": {
                            "id": 1,
                            "userId": 123,
                            "problemId": 456,
                            "videoId": "550e8400-e29b-41d4-a716-446655440000",
                            "rating": 5,
                            "holdColor": "RED",
                            "status": "APPROVED",
                            "submittedAt": "2024-01-01T09:00:00Z"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "비디오를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "비디오를 찾을 수 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "비디오를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    SubmissionResponseDto getSubmissionByVideoId(
        @Parameter(
            name = "videoId",
            description = "비디오 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID videoId
    );

    @Operation(
        summary = "제출물 취소",
        description = "특정 비디오 ID에 해당하는 제출물을 취소합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "제출물 취소 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "제출물 취소 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000",
                          "data": {
                            "videoId": "550e8400-e29b-41d4-a716-446655440000",
                            "canceledAt": "2024-01-01T10:00:00Z"
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
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "제출물에 대한 권한 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "제출물에 대한 권한 없음",
                    value = """
                        {
                          "httpStatus": 403,
                          "statusMessage": "제출물에 대한 권한이 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "비디오를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "비디오를 찾을 수 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "비디오를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    SubmissionCancelResponseDto cancelSubmission(
        Long userId,

        @Parameter(
            name = "videoId",
            description = "비디오 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID videoId
    );

    @Operation(
        summary = "제출물 이의신청 조회",
        description = "특정 비디오 ID에 해당하는 제출물의 이의신청을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이의신청 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "이의신청 조회 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 78,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
                          "data": {
                            "submissionId": 1,
                            "videoId": "550e8400-e29b-41d4-a716-446655440000",
                            "reason": "정당한 완등이었습니다.",
                            "status": "PENDING",
                            "appealedAt": "2024-01-01T10:00:00Z"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "비디오를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "비디오를 찾을 수 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "비디오를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    SubmissionAppealResponseDto getSubmissionAppeal(
        @Parameter(
            name = "videoId",
            description = "비디오 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID videoId
    );

    @Operation(
        summary = "제출물 이의신청",
        description = "특정 비디오 ID에 해당하는 제출물에 대해 이의신청을 합니다. 동일한 사유로는 중복 이의신청이 불가능합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "이의신청 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "이의신청 성공",
                    value = """
                        {
                          "httpStatus": 201,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
                          "data": {
                            "submissionId": 1,
                            "videoId": "550e8400-e29b-41d4-a716-446655440000",
                            "reason": "정당한 완등이었습니다.",
                            "status": "PENDING",
                            "appealedAt": "2024-01-01T10:00:00Z"
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
                          "statusMessage": "유효성 검사에 실패했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
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
                    name = "인증되지 않은 사용자",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "인증이 필요합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 23,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "제출물에 대한 권한 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "제출물에 대한 권한 없음",
                    value = """
                        {
                          "httpStatus": 403,
                          "statusMessage": "제출물에 대한 권한이 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "비디오를 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "비디오를 찾을 수 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "비디오를 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "동일한 사유로 이미 이의신청이 존재함",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "동일한 사유로 이미 이의신청이 존재함",
                    value = """
                        {
                          "httpStatus": 409,
                          "statusMessage": "이미 이의신청이 접수된 제출물입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/submissions/550e8400-e29b-41d4-a716-446655440000/appeal",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    SubmissionAppealResponseDto appealSubmission(
        Long userId,

        @Parameter(
            name = "videoId",
            description = "비디오 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID videoId,

        @Parameter(
            name = "reason",
            description = "이의신청 사유 (최대 256자, 선택사항)",
            example = "정당한 완등이었습니다."
        )
        @Valid
        SubmissionAppealRequestDto reason
    );
} 