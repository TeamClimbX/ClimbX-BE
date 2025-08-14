package com.climbx.climbx.submission.dto;

import com.climbx.climbx.submission.entity.SubmissionEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionCancelResponseDto(

    UUID videoId,
    LocalDateTime canceledAt
) {

    public static SubmissionCancelResponseDto from(SubmissionEntity submissionEntity) {
        return SubmissionCancelResponseDto.builder()
            .videoId(submissionEntity.videoId())
            .canceledAt(submissionEntity.updatedAt())
            .build();
    }
}