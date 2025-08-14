package com.climbx.climbx.video;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.video.dto.VideoListResponseDto;
import com.climbx.climbx.video.dto.VideoUploadRequestDto;
import com.climbx.climbx.video.dto.VideoUploadResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.validation.annotation.Validated;

/**
 * 영상 관련 API 문서화를 위한 인터페이스
 * <p>
 * 이 인터페이스는 영상 업로드와 관련된 API 엔드포인트들을 정의합니다. Swagger UI에서 "Video" 태그로 그룹화됩니다.
 */
@Validated
@Tag(name = "Video", description = "영상 업로드 관련 API")
public interface VideoApiDocumentation {

    @Operation(
        summary = "영상 업로드 URL 생성",
        description = "영상 업로드를 위한 presigned URL을 생성합니다. 생성된 URL을 통해 클라이언트는 직접 S3에 영상을 업로드할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "영상 업로드 URL 생성 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "영상 업로드 URL",
                    value = """
                        {
                          "httpStatus": 201,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 145,
                          "path": "/api/videos/upload",
                          "data": {
                            "videoId": "550e8400-e29b-41d4-a716-446655440000",
                            "presignedUrl": "https://climbx-video-bucket.s3.ap-northeast-2.amazonaws.com/550e8400-e29b-41d4-a716-446655440000.mp4?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240101T100000Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3600&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20240101%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Signature=example"
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
                    name = "인증 실패",
                    value = """
                        {
                          "httpStatus": 401,
                          "statusMessage": "인증이 필요합니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 45,
                          "path": "/api/videos/upload",
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
                          "path": "/api/videos/upload",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    VideoUploadResponseDto createVideoUploadUrl(
        Long userId,
        VideoUploadRequestDto videoUploadRequestDto
    );

    @Operation(
        summary = "영상 리스트 조회",
        description = "현재 로그인한 사용자가 업로드한 영상들의 썸네일과 메타데이터를 조회합니다. 삭제되지 않은 영상들만 생성날짜 내림차순으로 반환됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영상 리스트 조회 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "영상 리스트",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 89,
                          "path": "/api/videos",
                          "data": [
                            {
                              "thumbnailCdnUrl": "https://cdn.climbx.com/thumbnails/660e8400-e29b-41d4-a716-446655440001.jpg",
                              "hlsCdnUrl": "https://cdn.climbx.com/hls/660e8400-e29b-41d4-a716-446655440001/playlist.m3u8",
                              "status": "COMPLETED",
                              "durationSeconds": 40,
                              "createdAt": "2025-07-23T20:29:52.648189"
                            },
                            {
                              "thumbnailCdnUrl": "https://cdn.climbx.com/thumbnails/550e8400-e29b-41d4-a716-446655440000.jpg",
                              "hlsCdnUrl": "https://cdn.climbx.com/hls/550e8400-e29b-41d4-a716-446655440000/playlist.m3u8",
                              "status": "COMPLETED",
                              "durationSeconds": 10,
                              "createdAt": "2025-07-23T19:00:00.763233"
                            }
                          ]
                        }
                        """
                )
            )
        )
    })
    List<VideoListResponseDto> getVideoList(String nickname);
} 