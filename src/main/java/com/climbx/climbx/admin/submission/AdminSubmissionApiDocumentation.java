package com.climbx.climbx.admin.submission;

import com.climbx.climbx.admin.submission.dto.SubmissionReviewRequestDto;
import com.climbx.climbx.admin.submission.dto.SubmissionReviewResponseDto;
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
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;

@Validated
@Tag(name = "Admin Submission", description = "관리자 제출물 관리 API")
public interface AdminSubmissionApiDocumentation {

    @Operation(
        summary = "제출물 검토",
        description = "관리자가 특정 비디오 ID의 제출물을 검토하여 승인(ACCEPTED) 또는 거부(REJECTED) 처리합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "제출물 검토 완료",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "제출물 승인",
                        value = """
                            {
                              "httpStatus": 200,
                              "statusMessage": "SUCCESS",
                              "timeStamp": "2024-01-01T10:00:00Z",
                              "responseTimeMs": 156,
                              "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
                              "data": {
                                "videoId": "550e8400-e29b-41d4-a716-446655440000",
                                "status": "ACCEPTED",
                                "reason": "문제 해결 조건을 충족합니다."
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "제출물 거부",
                        value = """
                            {
                              "httpStatus": 200,
                              "statusMessage": "SUCCESS",
                              "timeStamp": "2024-01-01T10:00:00Z",
                              "responseTimeMs": 142,
                              "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
                              "data": {
                                "videoId": "550e8400-e29b-41d4-a716-446655440000",
                                "status": "REJECTED",
                                "reason": "홀드를 잘못 사용하였습니다."
                              }
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "유효성 검사 실패",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "유효성 검사에 실패했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 67,
                          "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
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
                          "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (관리자 권한 필요)",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "관리자 권한 없음",
                    value = """
                        {
                          "httpStatus": 403,
                          "statusMessage": "관리자 권한이 필요합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 34,
                          "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "제출물을 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "제출물 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "해당 비디오의 제출물을 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 검토된 제출물",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "이미 검토됨",
                    value = """
                        {
                          "httpStatus": 409,
                          "statusMessage": "이미 검토가 완료된 제출물입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
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
                          "path": "/api/admin/submissions/550e8400-e29b-41d4-a716-446655440000/status",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    SubmissionReviewResponseDto reviewSubmission(
        @Parameter(
            name = "videoId",
            description = "검토할 비디오의 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID videoId,

        @Parameter(
            description = "제출물 검토 요청 데이터",
            required = true
        )
        @Valid SubmissionReviewRequestDto request
    );
}
