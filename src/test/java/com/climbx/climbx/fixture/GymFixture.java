package com.climbx.climbx.fixture;

import com.climbx.climbx.gym.dto.GymInfoResponseDto;
import com.climbx.climbx.gym.entity.GymEntity;
import java.util.List;

public class GymFixture {

    public static final String ADDRESS = "서울시 마포구 공덕";
    public static final String PHONE_NUMBER = "02-1234-5678";

    public static GymEntity createGymEntity(Long gymId, String name, Double latitude,
        Double longitude) {
        return GymEntity.builder()
            .gymId(gymId)
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .address(ADDRESS)
            .phoneNumber(PHONE_NUMBER)
            .map2dImageCdnUrl("https://example.com/map2d.png")
            .gymAreas(List.of()) // Example areas, adjust as needed
            .build();
    }

    public static GymInfoResponseDto createGymInfoResponseDto(Long gymId, String name,
        Double latitude, Double longitude) {
        return GymInfoResponseDto.builder()
            .gymId(gymId)
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .address(ADDRESS)
            .phoneNumber(PHONE_NUMBER)
            .map2dImageCdnUrl("https://example.com/map2d.png")
            .gymAreas(List.of()) // Changed from null to empty list to match actual service behavior
            .build();
    }
}
