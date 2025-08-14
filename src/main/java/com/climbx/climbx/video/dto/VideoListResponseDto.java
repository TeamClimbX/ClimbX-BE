package com.climbx.climbx.video.dto;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.video.entity.VideoEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record VideoListResponseDto(

    UUID videoId,
    String thumbnailCdnUrl,
    String hlsCdnUrl,
    StatusType status,
    Integer durationSeconds,
    LocalDateTime createdAt
) {

    public static VideoListResponseDto from(VideoEntity videoEntity) {
        return VideoListResponseDto.builder()
            .videoId(videoEntity.videoId())
            .thumbnailCdnUrl(videoEntity.thumbnailCdnUrl())
            .hlsCdnUrl(videoEntity.hlsCdnUrl())
            .status(videoEntity.status())
            .durationSeconds(videoEntity.durationSeconds())
            .createdAt(videoEntity.createdAt())
            .build();
    }
}