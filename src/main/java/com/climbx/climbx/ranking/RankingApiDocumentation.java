package com.climbx.climbx.ranking;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.ranking.dto.RankingResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
@Tag(name = "Ranking", description = "랭킹 관련 API")
public interface RankingApiDocumentation {

    @Operation(
        operationId = "getRanking",
        summary = "사용자 랭킹 조회",
        description = """
            다양한 기준으로 사용자 랭킹을 조회합니다.
            
            **지원하는 랭킹 기준**:
            - `rating`: 레이팅 기준
            - `streak`: 연속 출석일 기준
            - `longestStreak`: 최장 연속 출석일 기준
            - `solvedCount`: 해결한 문제 수 기준
            
            **정렬 순서**:
            - `desc`: 내림차순 (기본값)
            - `asc`: 오름차순
            
            **주의사항**:
            - 관리자 계정은 랭킹에서 제외됩니다
            - 페이지 번호는 0부터 시작합니다
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "랭킹 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "레이팅 기준 랭킹 조회 성공",
                    value = """
                        {
                          "httpStatus": 200,
                          "statusMessage": "SUCCESS",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 145,
                          "path": "/api/ranking/users?criteria=rating&order=desc&page=0&perPage=10",
                          "data": {
                            "totalCount": 150,
                            "page": 0,
                            "perPage": 10,
                            "totalPage": 15,
                            "rankingList": [
                              {
                                "nickname": "ivan",
                                "statusMessage": "bouldering beast.",
                                "profileImageCdnUrl": "/images/ivan.png",
                                "rating": 3000,
                                "ranking": 1,
                                "currentStreak": 50,
                                "longestStreak": 50,
                                "solvedCount": 400
                              },
                              {
                                "nickname": "bob",
                                "statusMessage": "클라이밍 마스터가 되겠다!",
                                "profileImageCdnUrl": "https://example.com/profile/bob.jpg",
                                "rating": 1650,
                                "ranking": 2,
                                "currentStreak": 12,
                                "longestStreak": 30,
                                "solvedCount": 85
                              }
                            ]
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
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    name = "잘못된 랭킹 기준",
                    value = """
                        {
                          "httpStatus": 400,
                          "statusMessage": "잘못된 랭킹 기준입니다.",
                          "timeStamp": "2024-01-01T10:00:00Z",
                          "responseTimeMs": 87,
                          "path": "/api/ranking/users?criteria=invalid_criteria",
                          "data": null,
                          "errors": {
                            "criteria": "invalid_criteria"
                          }
                        }
                        """
                )
            )
        )
    })
    RankingResponseDto getRanking(
        @Parameter(
            name = "criteria",
            description = "랭킹 기준 (rating, streak, longestStreak, solvedCount)",
            required = true,
            example = "rating"
        )
        @NotNull(message = "랭킹 기준은 필수입니다")
        CriteriaType criteria,

        @Parameter(
            name = "pageable",
            description = "페이지 정보",
            required = true,
            example = "page=0&size=20"
        )
        Pageable pageable
    );
} 