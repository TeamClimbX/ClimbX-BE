package com.climbx.climbx.video.dto;

import java.util.UUID;
import lombok.Builder;

/**
 * 비디오 삭제 응답 DTO
 */
@Builder
public record VideoDeleteResponseDto(
    UUID videoId,
    String message
) {

    public static VideoDeleteResponseDto from(UUID videoId) {
        return VideoDeleteResponseDto.builder()
            .videoId(videoId)
            .message("비디오가 성공적으로 삭제되었습니다.")
            .build();
    }
}
