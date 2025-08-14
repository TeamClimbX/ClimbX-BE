package com.climbx.climbx.admin.gym;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.gym.dto.GymAreaInfoResponseDto;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
@Tag(name = "Admin Gym", description = "관리자 클라이밍장 관리 API")
public interface AdminGymApiDocumentation {

    @Operation(
        summary = "클라이밍장 구역 등록",
        description = "관리자가 특정 클라이밍장에 새로운 구역(벽)을 등록합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "구역 등록 완료",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "구역 등록 성공",
                    value = """
                        {
                          "httpStatus": 201,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 234,
                          "path": "/api/admin/gyms/1/areas",
                          "data": {
                            "areaId": 1,
                            "areaName": "볼더링 A구역",
                            "areaImageCdnUrl": null
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
                    name = "유효성 검증 실패",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "구역 이름은 필수입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/admin/gyms/1/areas",
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
                          "path": "/api/admin/gyms/1/areas",
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
                          "path": "/api/admin/gyms/1/areas",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "클라이밍장을 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "클라이밍장 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "해당 클라이밍장을 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/admin/gyms/1/areas",
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
                          "path": "/api/admin/gyms/1/areas",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    GymAreaInfoResponseDto createGymArea(
        @Parameter(
            name = "gymId",
            description = "구역을 등록할 클라이밍장의 ID",
            required = true,
            example = "1"
        )
        Long gymId,

        @Parameter(
            name = "areaName",
            description = "구역 이름",
            required = true
        )
        @Valid
        String areaName,

        @Parameter(
            name = "areaImage",
            description = "구역 이미지 파일 (PNG, JPG, JPEG 지원, 선택사항)",
            required = false
        )
        MultipartFile areaImage
    );

    @Operation(
        summary = "클라이밍장 2D 맵 이미지 업데이트",
        description = "관리자가 클라이밍장의 2D 맵 이미지를 업데이트합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "2D 맵 이미지 업데이트 완료"
        ),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "클라이밍장을 찾을 수 없음")
    })
    void updateGymMap2dImage(
        @Parameter(
            name = "gymId",
            description = "2D 맵을 업데이트할 클라이밍장의 ID",
            required = true,
            example = "1"
        )
        Long gymId,

        @Parameter(
            name = "map2dImage",
            description = "클라이밍장의 2D 맵 이미지 파일 (PNG, JPG, JPEG 지원)",
            required = true
        )
        MultipartFile map2dImage
    );

    @Operation(
        summary = "클라이밍장 구역 이미지 업데이트",
        description = "관리자가 특정 클라이밍장 구역의 이미지를 업데이트합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "구역 이미지 업데이트 완료"
        ),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "구역을 찾을 수 없음")
    })
    void updateGymAreaImage(
        @Parameter(
            name = "gymId",
            description = "클라이밍장의 ID",
            required = true,
            example = "1"
        )
        Long gymId,

        @Parameter(
            name = "areaId",
            description = "업데이트할 구역의 ID",
            required = true,
            example = "1"
        )
        Long areaId,

        @Parameter(
            name = "areaImage",
            description = "구역 이미지 파일 (PNG, JPG, JPEG 지원)",
            required = true
        )
        MultipartFile areaImage
    );
}