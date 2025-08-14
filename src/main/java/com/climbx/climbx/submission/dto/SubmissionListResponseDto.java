package com.climbx.climbx.submission.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record SubmissionListResponseDto(

    List<SubmissionResponseDto> submissions,
    Long totalCount,
    Boolean hasNext,
    String nextCursor
) {

    public static SubmissionListResponseDto from(
        List<SubmissionResponseDto> submissions,
        Long totalCount,
        Boolean hasNext,
        String nextCursor
    ) {
        return SubmissionListResponseDto.builder()
            .submissions(submissions)
            .totalCount(totalCount)
            .hasNext(hasNext)
            .nextCursor(nextCursor)
            .build();
    }
} 