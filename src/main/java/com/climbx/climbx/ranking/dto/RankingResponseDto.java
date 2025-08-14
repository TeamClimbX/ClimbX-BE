package com.climbx.climbx.ranking.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record RankingResponseDto(

    List<UserRankingResponseDto> rankings, // 랭킹 리스트
    Long totalCount,
    Boolean hasNext,
    String nextCursor
) {

}
