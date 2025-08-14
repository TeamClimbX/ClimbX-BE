package com.climbx.climbx.admin.gym;

import com.climbx.climbx.admin.gym.service.AdminGymService;
import com.climbx.climbx.common.annotation.SuccessStatus;
import com.climbx.climbx.gym.dto.GymAreaInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/admin/gyms")
@RequiredArgsConstructor
public class AdminGymController implements AdminGymApiDocumentation {

    private final AdminGymService adminGymService;

    @Override
    @PostMapping("/{gymId}/2d-map")
    @SuccessStatus(HttpStatus.OK)
    public void updateGymMap2dImage(
        @PathVariable(name = "gymId") Long gymId,
        @RequestPart(name = "map2dImage") MultipartFile map2dImage
    ) {
        log.info("클라이밍장 2D 맵 이미지 업데이트 요청: gymId={}, fileName={}",
            gymId, map2dImage.getOriginalFilename());

        adminGymService.updateGymMap2dImage(gymId, map2dImage);

        log.info("클라이밍장 2D 맵 이미지 업데이트 완료: gymId={}", gymId);
    }

    @Override
    @PostMapping("/{gymId}/areas")
    @SuccessStatus(HttpStatus.CREATED)
    public GymAreaInfoResponseDto createGymArea(
        @PathVariable(name = "gymId") Long gymId,
        @RequestPart(name = "areaName", required = true) String areaName,
        @RequestPart(name = "areaImage", required = false) MultipartFile areaImage
    ) {
        log.info("클라이밍장 구역 등록 요청: gymId={}, areaName={}, hasImage={}",
            gymId, areaName, areaImage != null);

        GymAreaInfoResponseDto response = adminGymService.createGymArea(gymId, areaName, areaImage);

        log.info("클라이밍장 구역 등록 완료: gymId={}, areaId={}, areaName={}",
            gymId, response.areaId(), response.areaName());

        return response;
    }

    @Override
    @PostMapping("/{gymId}/areas/{areaId}/image")
    @SuccessStatus(HttpStatus.OK)
    public void updateGymAreaImage(
        @PathVariable(name = "gymId") Long gymId,
        @PathVariable(name = "areaId") Long areaId,
        @RequestPart(name = "areaImage") MultipartFile areaImage
    ) {
        log.info("클라이밍장 구역 이미지 업데이트 요청: gymId={}, areaId={}, fileName={}",
            gymId, areaId, areaImage.getOriginalFilename());

        adminGymService.updateGymAreaImage(gymId, areaId, areaImage);

        log.info("클라이밍장 구역 이미지 업데이트 완료: gymId={}, areaId={}", gymId, areaId);
    }
}
