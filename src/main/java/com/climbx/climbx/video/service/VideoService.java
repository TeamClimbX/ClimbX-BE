package com.climbx.climbx.video.service;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.video.dto.VideoDeleteResponseDto;
import com.climbx.climbx.video.dto.VideoListResponseDto;
import com.climbx.climbx.video.dto.VideoUploadRequestDto;
import com.climbx.climbx.video.dto.VideoUploadResponseDto;
import com.climbx.climbx.video.entity.VideoEntity;
import com.climbx.climbx.video.exception.VideoNotFoundException;
import com.climbx.climbx.video.exception.VideoOnlyOwnerCanModifyException;
import com.climbx.climbx.video.repository.VideoRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final S3Service s3Service;
    private final VideoRepository videoRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public VideoUploadResponseDto createVideoUploadUrl(
        Long userId,
        VideoUploadRequestDto videoUploadRequestDto
    ) {
        UserAccountEntity user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        UUID videoId = UUID.randomUUID();
        String presignedUrl = s3Service.generateVideoUploadPresignedUrl(
            videoId,
            videoUploadRequestDto.fileExtension()
        );

        VideoEntity videoEntity = VideoEntity.builder()
            .videoId(videoId)
            .userId(userId)
            .userAccountEntity(user)
            .fileSize(videoUploadRequestDto.fileSize())
            .status(StatusType.PENDING)
            .build();

        videoRepository.save(videoEntity);

        return VideoUploadResponseDto.builder()
            .videoId(videoId)
            .presignedUrl(presignedUrl)
            .build();
    }

    @Transactional(readOnly = true)
    public List<VideoListResponseDto> getVideoList(String nickname) {
        UserAccountEntity user = userAccountRepository.findByNickname(nickname)
            .orElseThrow(() -> new UserNotFoundException(nickname));

        return videoRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                user.userId(),
                List.of(StatusType.PENDING, StatusType.COMPLETED)
            )
            .stream()
            .map(VideoListResponseDto::from)
            .toList();
    }

    @Transactional
    public VideoDeleteResponseDto deleteVideo(Long userId, UUID videoId) {
        VideoEntity videoEntity = videoRepository.findById(videoId)
            .orElseThrow(() -> new VideoNotFoundException(videoId));

        // 비디오 소유자 확인
        if (!videoEntity.userId().equals(userId)) {
            throw new VideoOnlyOwnerCanModifyException(ErrorCode.VIDEO_ONLY_OWNER_CAN_MODIFY);
        }

        // Soft delete 수행
        videoEntity.softDelete();

        return VideoDeleteResponseDto.from(videoId);
    }
}
