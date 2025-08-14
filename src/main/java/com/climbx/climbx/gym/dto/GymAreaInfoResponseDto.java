package com.climbx.climbx.gym.dto;

import com.climbx.climbx.gym.entity.GymAreaEntity;
import lombok.Builder;

@Builder
public record GymAreaInfoResponseDto(

    Long areaId,
    String areaName,
    String areaImageCdnUrl
) {

    public static GymAreaInfoResponseDto from(GymAreaEntity gymAreaEntity) {
        return GymAreaInfoResponseDto.builder()
            .areaId(gymAreaEntity.gymAreaId())
            .areaName(gymAreaEntity.areaName())
            .areaImageCdnUrl(gymAreaEntity.areaImageCdnUrl())
            .build();
    }
}
