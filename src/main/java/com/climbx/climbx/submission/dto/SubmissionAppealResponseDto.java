package com.climbx.climbx.submission.dto;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionAppealResponseDto(

    UUID videoId,
    String appealReason,
    StatusType appealStatus,
    LocalDateTime appealedAt
) {

    public static SubmissionAppealResponseDto from(SubmissionEntity submissionEntity) {
        return SubmissionAppealResponseDto.builder()
            .videoId(submissionEntity.videoId())
            .appealReason(submissionEntity.appealContent())
            .appealStatus(submissionEntity.appealStatus())
            .appealedAt(
                submissionEntity.appealContent() != null
                    ? submissionEntity.updatedAt()
                    : null)
            .build();
    }
}