package com.climbx.climbx.admin.scheduler;

import com.climbx.climbx.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Scheduler", description = "개발 환경용 스케줄러 수동 실행 API")
public interface AdminSchedulerApiDocumentation {

    @Operation(
        summary = "Outbox 이벤트 스케줄러 수동 실행",
        description = "개발 환경에서 OutboxEventScheduler를 수동으로 실행합니다. 스케줄러 내부에서 로그를 처리하며, 에러 발생 시 적절한 예외가 throw됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "스케줄러 실행 완료",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/admin/scheduler/outbox/trigger",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "스케줄러 실행 중 오류 발생",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "실행 오류",
                    value = """
                        {
                          "httpStatus": 500,
                          "statusMessage": "서버 내부 오류가 발생했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/admin/scheduler/outbox/trigger",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    void triggerOutboxEventScheduler();

    @Operation(
        summary = "일일 랭킹 스냅샷 스케줄러 수동 실행",
        description = "개발 환경에서 DailyRankingSnapshotScheduler를 수동으로 실행합니다. 스케줄러 내부에서 로그를 처리하며, 에러 발생 시 적절한 예외가 throw됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "스케줄러 실행 완료",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 342,
                          "path": "/api/admin/scheduler/ranking/trigger",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "스케줄러 실행 중 오류 발생",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "실행 오류",
                    value = """
                        {
                          "httpStatus": 500,
                          "statusMessage": "서버 내부 오류가 발생했습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 234,
                          "path": "/api/admin/scheduler/ranking/trigger",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    void triggerDailyRankingSnapshotScheduler();
}