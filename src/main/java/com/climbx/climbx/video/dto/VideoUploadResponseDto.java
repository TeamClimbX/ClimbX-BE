package com.climbx.climbx.video.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record VideoUploadResponseDto(
    UUID videoId,
    String presignedUrl
) {

}