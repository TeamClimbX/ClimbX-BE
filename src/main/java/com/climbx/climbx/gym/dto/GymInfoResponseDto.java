package com.climbx.climbx.gym.dto;

import com.climbx.climbx.gym.entity.GymEntity;
import java.util.List;
import lombok.Builder;

@Builder
public record GymInfoResponseDto(

    Long gymId,
    String name,
    Double latitude,
    Double longitude,
    String address,
    String phoneNumber,
    String map2dImageCdnUrl,
    List<GymAreaInfoResponseDto> gymAreas
) {

    public static GymInfoResponseDto from(GymEntity gym) {
        return GymInfoResponseDto.builder()
            .gymId(gym.gymId())
            .name(gym.name())
            .latitude(gym.latitude())
            .longitude(gym.longitude())
            .address(gym.address())
            .phoneNumber(gym.phoneNumber())
            .map2dImageCdnUrl(gym.map2dImageCdnUrl())
            .gymAreas(gym.gymAreas().stream()       // Todo refactoring
                .map(GymAreaInfoResponseDto::from)
                .toList())
            .build();
    }
}
