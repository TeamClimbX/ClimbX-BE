package com.climbx.climbx.gym;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.gym.dto.GymInfoResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
@Tag(name = "Gym", description = "클라이밍장 관련 API")
public interface GymApiDocumentation {

    @Operation(
        operationId = "getGymById",
        summary = "클라이밍장 정보 조회",
        description = "클라이밍장 ID를 사용하여 특정 클라이밍장의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "클라이밍장 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/gyms/1",
                          "data": {
                            "gymId": 1,
                            "name": "클라이밍 파크",
                            "latitude": 37.5665,
                            "longitude": 126.9780,
                            "address": "서울시 강남구 테헤란로 123",
                            "phoneNumber": "02-1234-5678",
                            "baseMapUrl": "https://cdn-url/2d-map/1/base-images/1-base.png",
                            "overlayMapUrls": [
                                "https://cdn-url/2d-map/1/overlay-images/1-사과.jpeg",
                                "https://cdn-url/2d-map/1/overlay-images/1-배1.png"
                            ]
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 클라이밍장 ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 클라이밍장 ID입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 123,
                          "path": "/api/gyms/0",
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
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "클라이밍장 없음",
                    value = """
                        {
                          "httpStatus": 404,
                          "statusMessage": "클라이밍장을 찾을 수 없습니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 98,
                          "path": "/api/gyms/999",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    GymInfoResponseDto getGymById(
        @Parameter(
            name = "gymId",
            description = "클라이밍장 ID",
            required = true,
            example = "1"
        )
        @NotNull
        @Min(1L)
        Long gymId
    );

    @Operation(
        operationId = "getGymListByKeyword",
        summary = "키워드 기반 클라이밍장 목록 조회",
        description = """
            키워드를 사용하여 클라이밍장 목록을 조회합니다. 키워드가 없으면 모든 클라이밍장을 반환합니다.
            
            **요청 예시**:
            - GET /api/gyms/search
            - GET /api/gyms/search?keyword=클라임
            
            **참고**: 거리 기반 검색을 원하면 /api/gyms/nearby 엔드포인트를 사용하세요.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "클라이밍장 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "키워드 기반 클라이밍장 목록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 156,
                          "path": "/api/gyms/search?keyword=클라임",
                          "data": [
                            {
                              "gymId": 1,
                              "name": "클라이밍 파크",
                              "latitude": 37.5665,
                              "longitude": 126.9780,
                              "address": "서울시 강남구 테헤란로 123",
                              "phoneNumber": "02-1234-5678",
                              "baseMapUrl": "https://cdn-url/2d-map/1/base-images/1-base.png",
                              "overlayMapUrls": [
                                  "https://cdn-url/2d-map/1/overlay-images/1-사과.jpeg",
                                  "https://cdn-url/2d-map/1/overlay-images/1-배1.png"
                              ]
                            },
                            {
                              "gymId": 2,
                              "name": "더 클라임",
                              "latitude": 37.4845,
                              "longitude": 127.0320,
                              "address": "서울시 서초구 강남대로 456",
                              "phoneNumber": "02-9876-5432",
                              "baseMapUrl": "https://cdn-url/2d-map/1/base-images/1-base.png",
                              "overlayMapUrls": [
                                  "https://cdn-url/2d-map/1/overlay-images/1-사과.jpeg",
                                  "https://cdn-url/2d-map/1/overlay-images/1-배1.png"
                              ]
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 요청 파라미터입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 87,
                          "path": "/api/gyms/search",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<GymInfoResponseDto> getGymList(
        @Parameter(
            name = "keyword",
            description = "검색 키워드 (클라이밍장 이름, 주소 등)",
            required = false,
            example = "클라임"
        )
        String keyword
    );

    @Operation(
        operationId = "getGymListByDistance",
        summary = "거리 기반 클라이밍장 목록 조회",
        description = """
            사용자의 현재 위치(위도, 경도)를 기준으로 거리 순으로 클라이밍장 목록을 조회합니다.
            
            **요청 예시**:
            - GET /api/gyms/nearby?latitude=37.5665&longitude=126.9780
            - GET /api/gyms/nearby?latitude=37.5665&longitude=126.9780&keyword=클라임
            
            **참고**: 키워드 검색만 원하면 /api/gyms/search 엔드포인트를 사용하세요.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "거리 기반 클라이밍장 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "거리순 클라이밍장 목록",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 187,
                          "path": "/api/gyms/nearby?latitude=37.5665&longitude=126.9780",
                          "data": [
                            {
                              "gymId": 1,
                              "name": "클라이밍 파크",
                              "latitude": 37.5665,
                              "longitude": 126.9780,
                              "address": "서울시 강남구 테헤란로 123",
                              "phoneNumber": "02-1234-5678",
                              "baseMapUrl": "https://cdn-url/2d-map/1/base-images/1-base.png",
                              "overlayMapUrls": [
                                  "https://cdn-url/2d-map/1/overlay-images/1-사과.jpeg",
                                  "https://cdn-url/2d-map/1/overlay-images/1-배1.png"
                              ]
                            },
                            {
                              "gymId": 3,
                              "name": "하이클라임",
                              "latitude": 37.5012,
                              "longitude": 127.0396,
                              "address": "서울시 강남구 역삼로 789",
                              "phoneNumber": "02-5555-1234",
                              "baseMapUrl": "https://cdn-url/2d-map/1/base-images/1-base.png",
                              "overlayMapUrls": [
                                  "https://cdn-url/2d-map/1/overlay-images/1-사과.jpeg",
                                  "https://cdn-url/2d-map/1/overlay-images/1-배1.png"
                              ]
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 위도 또는 경도",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 위치 정보",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 위도 또는 경도 값입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 112,
                          "path": "/api/gyms/nearby?latitude=200&longitude=300",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    List<GymInfoResponseDto> getGymListByDistance(
        @Parameter(
            name = "latitude",
            description = "위도 (-90.0 ~ 90.0)",
            required = true,
            example = "37.5665"
        )
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        Double latitude,
        @Parameter(
            name = "longitude",
            description = "경도 (-180.0 ~ 180.0)",
            required = true,
            example = "126.9780"
        )
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", inclusive = false, message = "Longitude must be between -180 and 180")
        Double longitude,
        @Parameter(
            name = "keyword",
            description = "검색 키워드 (클라이밍장 이름, 주소 등)",
            required = false,
            example = "클라임"
        )
        String keyword
    );
} 