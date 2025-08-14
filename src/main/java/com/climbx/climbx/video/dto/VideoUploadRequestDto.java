package com.climbx.climbx.video.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record VideoUploadRequestDto(

    @NotBlank
    String fileExtension, // 파일 확장자 (예: mp4, mov 등)

    @NotNull
    @Min(value = 1)
    @Max(value = 1024 * 1024 * 1024) // 최대 1GB
    Long fileSize // 파일 크기 (바이트 단위)
) {

}