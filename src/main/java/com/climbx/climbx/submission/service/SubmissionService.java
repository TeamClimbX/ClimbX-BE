package com.climbx.climbx.submission.service;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.exception.ProblemNotFoundException;
import com.climbx.climbx.problem.repository.ProblemRepository;
import com.climbx.climbx.submission.dto.SubmissionAppealRequestDto;
import com.climbx.climbx.submission.dto.SubmissionAppealResponseDto;
import com.climbx.climbx.submission.dto.SubmissionCancelResponseDto;
import com.climbx.climbx.submission.dto.SubmissionCreateRequestDto;
import com.climbx.climbx.submission.dto.SubmissionListResponseDto;
import com.climbx.climbx.submission.dto.SubmissionResponseDto;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.submission.exception.DuplicateAppealException;
import com.climbx.climbx.submission.exception.DuplicateSubmissionException;
import com.climbx.climbx.submission.exception.ForbiddenSubmissionException;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.video.entity.VideoEntity;
import com.climbx.climbx.video.exception.VideoNotFoundException;
import com.climbx.climbx.video.repository.VideoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final VideoRepository videoRepository;
    private final ProblemRepository problemRepository;
    private final UserAccountRepository userAccountRepository;

    public SubmissionListResponseDto getSubmissions(
        String nickname,
        Long problemId,
        String holdColor,
        Integer ratingFrom,
        Integer ratingTo,
        Pageable pageable
    ) {
        UserAccountEntity user = userAccountRepository.findByNickname(nickname)
            .orElseThrow(() -> new UserNotFoundException(nickname));

        Long userId = user.userId();

        // 제출 목록 조회 (Page 객체로 반환)
        Page<SubmissionEntity> submissionPage = submissionRepository.findSubmissionsWithFilters(
            userId, problemId, holdColor, ratingFrom, ratingTo, pageable
        );

        // Page에서 정보 추출
        List<SubmissionResponseDto> submissions = submissionPage.getContent().stream()
            .map(SubmissionResponseDto::from)
            .toList();

        // 페이징 정보 계산
        Long totalCount = submissionPage.getTotalElements();
        Boolean hasNext = submissionPage.hasNext();
        String nextCursor = hasNext
            ? submissionPage.nextPageable().getPageNumber() + ""
            : null;

        return SubmissionListResponseDto.builder()
            .submissions(submissions)
            .totalCount(totalCount)
            .hasNext(hasNext)
            .nextCursor(nextCursor)
            .build();
    }

    @Transactional
    public SubmissionResponseDto createSubmission(Long userId, SubmissionCreateRequestDto request) {
        VideoEntity video = videoRepository.findByVideoIdAndStatus(
            request.videoId(),
            StatusType.COMPLETED
        ).orElseThrow(() -> new VideoNotFoundException(request.videoId()));

        if (!video.userId().equals(userId)) {
            log.warn("User {} attempted to submit video {} they do not own", userId,
                request.videoId());
            throw new ForbiddenSubmissionException(userId, request.videoId());
        }

        // 이미 제출된 영상인지 확인
        submissionRepository.findById(request.videoId())
            .ifPresent(existingSubmission -> {
                throw new DuplicateSubmissionException(request.videoId());
            });

        ProblemEntity problem = problemRepository.findById(request.problemId())
            .orElseThrow(() -> new ProblemNotFoundException(request.problemId()));

        SubmissionEntity submissionEntity = SubmissionEntity.builder()
            .videoEntity(video)
            .problemEntity(problem)
            .status(StatusType.PENDING)
            .build();

        submissionRepository.save(submissionEntity);

        UserAccountEntity user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        UserStatEntity userStat = user.userStatEntity();
        userStat.incrementSubmissionCount();

        return SubmissionResponseDto.from(submissionEntity);
    }

    public SubmissionResponseDto getSubmissionByVideoId(UUID videoId) {
        SubmissionEntity submissionEntity = submissionRepository.findById(videoId)
            .orElseThrow(() -> new VideoNotFoundException(videoId));

        return SubmissionResponseDto.from(submissionEntity);
    }

    @Transactional
    public SubmissionCancelResponseDto cancelSubmission(Long userId, UUID videoId) {
        SubmissionEntity submissionEntity = submissionRepository.findById(videoId)
            .orElseThrow(() -> new VideoNotFoundException(videoId));

        Optional.of(submissionEntity.videoEntity().userId())
            .filter(id -> !id.equals(userId)).ifPresent(id -> {
                throw new ForbiddenSubmissionException(userId, videoId);
            });

        submissionEntity.softDelete();

        return SubmissionCancelResponseDto.from(submissionEntity);
    }

    public SubmissionAppealResponseDto getSubmissionAppeal(UUID videoId) {
        SubmissionEntity submissionEntity = submissionRepository.findById(videoId)
            .orElseThrow(() -> new VideoNotFoundException(videoId));

        return SubmissionAppealResponseDto.from(submissionEntity);
    }

    @Transactional
    public SubmissionAppealResponseDto appealSubmission(Long userId, UUID videoId,
        SubmissionAppealRequestDto request) {
        SubmissionEntity submissionEntity = submissionRepository.findById(videoId)
            .orElseGet(() -> {
                log.warn("User {} attempted to appeal submission for non-existent video {}", userId,
                    videoId);
                throw new VideoNotFoundException(videoId);
            });

        Optional.of(submissionEntity.videoEntity().userId())
            .filter(id -> !id.equals(userId)).ifPresent(id -> {
                log.warn("User {} attempted to appeal submission for video {} they do not own", userId,
                    videoId);
                throw new ForbiddenSubmissionException(userId, videoId);
            });

        Optional.ofNullable(submissionEntity.appealContent())
            .filter(content -> content.equals(request.reason()))
            .ifPresent(content -> {
                log.warn(
                    "User {} attempted to appeal submission for video {} with duplicate reason",
                    userId, videoId);
                throw new DuplicateAppealException(videoId);
            });

        submissionEntity.setAppealContent(request.reason());

        return SubmissionAppealResponseDto.from(submissionEntity);
    }
}