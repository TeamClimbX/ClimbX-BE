package com.climbx.climbx.gym;

import com.climbx.climbx.common.annotation.SuccessStatus;
import com.climbx.climbx.gym.dto.GymInfoResponseDto;
import com.climbx.climbx.gym.service.GymService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
public class GymController implements GymApiDocumentation {

    private final GymService gymService;

    @Override
    @GetMapping("/{gymId}")
    @SuccessStatus(value = HttpStatus.OK)
    public GymInfoResponseDto getGymById(@PathVariable Long gymId) {
        log.info("ID로 클라이밍장 조회: {}", gymId);
        return gymService.getGymById(gymId);
    }

    @Override
    @GetMapping("/search")
    @SuccessStatus(value = HttpStatus.OK)
    public List<GymInfoResponseDto> getGymList(@RequestParam(required = false) String keyword) {
        log.info("키워드로 클라이밍장 목록 조회: {}", keyword);
        return gymService.getGymList(keyword);
    }

    @Override
    @GetMapping("/nearby")
    @SuccessStatus(value = HttpStatus.OK)
    public List<GymInfoResponseDto> getGymListByDistance(
        @RequestParam
        Double latitude,

        @RequestParam
        Double longitude,

        @RequestParam(required = false)
        String keyword
    ) {
        log.info("위치 기반 클라이밍장 목록 조회: latitude={}, longitude={}, keyword={}",
            latitude, longitude, keyword);
        return gymService.getGymListByDistance(latitude, longitude, keyword);
    }
}
