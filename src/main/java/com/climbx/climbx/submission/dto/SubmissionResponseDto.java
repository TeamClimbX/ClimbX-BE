package com.climbx.climbx.submission.dto;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubmissionResponseDto(

    UUID videoId,
    UUID problemId,
    String problemLocalLevel,
    String problemHoldColor,
    Integer problemRating,
    String gymName,
    StatusType status,
    String userNickname,
    String thumbnailUrl,
    Integer durationSeconds,
    LocalDateTime submittedAt,
    LocalDateTime updatedAt
) {

    public static SubmissionResponseDto from(SubmissionEntity submissionEntity) {
        return SubmissionResponseDto.builder()
            .videoId(submissionEntity.videoId())
            .problemId(submissionEntity.problemId())
            .problemLocalLevel(submissionEntity.problemEntity().localLevel())
            .problemHoldColor(submissionEntity.problemEntity().holdColor())
            .problemRating(submissionEntity.problemEntity().rating())
            .gymName(submissionEntity.problemEntity().gymEntity().name())
            .status(submissionEntity.status())
            .userNickname(submissionEntity.videoEntity().userAccountEntity().nickname())
            .thumbnailUrl(submissionEntity.videoEntity().thumbnailCdnUrl())
            .durationSeconds(submissionEntity.videoEntity().durationSeconds())
            .submittedAt(submissionEntity.createdAt())
            .updatedAt(submissionEntity.updatedAt())
            .build();
    }
} 