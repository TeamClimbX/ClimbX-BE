package com.climbx.climbx.admin.gym.service;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.InvalidParameterException;
import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.gym.dto.GymAreaInfoResponseDto;
import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.gym.exception.GymNotFoundException;
import com.climbx.climbx.gym.repository.GymAreaRepository;
import com.climbx.climbx.gym.repository.GymRepository;
import com.climbx.climbx.problem.exception.GymAreaNotFoundException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGymService {

    private final S3Service s3Service;
    private final GymRepository gymRepository;
    private final GymAreaRepository gymAreaRepository;

    @Transactional
    public GymAreaInfoResponseDto createGymArea(Long gymId, String areaName,
        MultipartFile areaImage) {
        log.info("클라이밍장 구역 생성 시작: gymId={}, areaName={}, hasImage={}",
            gymId, areaName, areaImage != null);

        // 클라이밍장 존재 여부 확인
        GymEntity gym = gymRepository.findById(gymId)
            .orElseThrow(() -> new GymNotFoundException(gymId));

        // 새로운 구역 엔티티 생성
        GymAreaEntity newGymArea = GymAreaEntity.builder()
            .gym(gym)
            .areaName(areaName)
            .build();

        // 구역 저장
        GymAreaEntity savedGymArea = gymAreaRepository.save(newGymArea);

        // 이미지가 제공되었다면 S3에 업로드 후 URL 저장
        if (areaImage != null && !areaImage.isEmpty()) {
            Map<Long, MultipartFile> areaImageMap = Map.of(savedGymArea.gymAreaId(), areaImage);
            Map<Long, String> areaImageCdnUrls = s3Service.uploadGymAreaImages(gymId, areaImageMap);
            String areaImageCdnUrl = areaImageCdnUrls.get(savedGymArea.gymAreaId());
            savedGymArea.setAreaImageCdnUrl(areaImageCdnUrl);
        }

        log.info("클라이밍장 구역 생성 완료: gymId={}, areaId={}, areaName={}",
            gymId, savedGymArea.gymAreaId(), savedGymArea.areaName());

        return GymAreaInfoResponseDto.from(savedGymArea);
    }

    @Transactional
    public void updateGymMap2dImage(Long gymId, MultipartFile map2dImage) {
        log.info("클라이밍장 2D 맵 이미지 업데이트 시작: gymId={}, fileName={}",
            gymId, map2dImage.getOriginalFilename());

        // 클라이밍장 존재 여부 확인
        GymEntity gym = gymRepository.findById(gymId)
            .orElseThrow(() -> new GymNotFoundException(gymId));

        // S3에 클라이밍장 2D 맵 이미지 업로드
        String map2dImageCdnUrl = s3Service.uploadGym2dMapImages(gymId, map2dImage);
        gym.setMap2dImageCdnUrl(map2dImageCdnUrl);

        log.info("클라이밍장 2D 맵 이미지 업데이트 완료: gymId={}, cdnUrl={}", gymId, map2dImageCdnUrl);
    }

    @Transactional
    public void updateGymAreaImage(Long gymId, Long areaId, MultipartFile areaImage) {
        log.info("클라이밍장 구역 이미지 업데이트 시작: gymId={}, areaId={}, fileName={}",
            gymId, areaId, areaImage.getOriginalFilename());

        // 클라이밍장 존재 여부 확인
        gymRepository.findById(gymId)
            .orElseThrow(() -> new GymNotFoundException(gymId));

        // 구역 존재 여부 확인 및 해당 클라이밍장에 속하는지 검증
        GymAreaEntity gymArea = gymAreaRepository.findById(areaId)
            .orElseThrow(() -> new GymAreaNotFoundException(areaId));

        if (!gymArea.gym().gymId().equals(gymId)) {
            throw new InvalidParameterException(ErrorCode.INVALID_PARAMETER,
                String.format("구역 ID %d는 클라이밍장 ID %d에 속하지 않습니다.", areaId, gymId));
        }

        // S3에 구역 이미지 업로드
        Map<Long, MultipartFile> areaImageMap = Map.of(areaId, areaImage);
        Map<Long, String> areaImageCdnUrls = s3Service.uploadGymAreaImages(gymId, areaImageMap);
        String areaImageCdnUrl = areaImageCdnUrls.get(areaId);
        gymArea.setAreaImageCdnUrl(areaImageCdnUrl);

        log.info("클라이밍장 구역 이미지 업데이트 완료: gymId={}, areaId={}, cdnUrl={}",
            gymId, areaId, areaImageCdnUrl);
    }
}
