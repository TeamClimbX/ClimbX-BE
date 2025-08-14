package com.climbx.climbx.admin.submission.service;

import com.climbx.climbx.admin.submission.dto.SubmissionReviewRequestDto;
import com.climbx.climbx.admin.submission.dto.SubmissionReviewResponseDto;
import com.climbx.climbx.admin.submission.exception.StatusModifyToPendingException;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.user.util.UserRatingUtil;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.submission.exception.PendingSubmissionNotFoundException;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.RatingResponseDto;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AdminSubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserStatRepository userStatRepository;
    private final UserRatingUtil userRatingUtil;

    @Transactional
    public SubmissionReviewResponseDto reviewSubmission(
        UUID videoId,
        SubmissionReviewRequestDto request
    ) {
        // 검토 요청 상태가 PENDING인 경우 예외 처리
        if (request.status() == StatusType.PENDING) {
            log.warn("Status를 PENDING으로 변경 시도 videoId: {}, status: {}, reason: {}",
                videoId, request.status(), request.reason());
            throw new StatusModifyToPendingException(videoId);
        }

        // PENDING 상태의 제출 조회
        SubmissionEntity submission = submissionRepository.findById(videoId)
            .filter(s -> s.status().equals(StatusType.PENDING))
            .orElseThrow(() -> new PendingSubmissionNotFoundException(videoId));

        submission.setStatus(request.status(), request.reason());

        log.info("Reviewing succeed: videoId: {}, status: {}, reason: {}",
            submission.videoId(), submission.status(), submission.statusReason());

        Long userId = submission.videoEntity().userId();

        UserStatEntity userStat = userStatRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        int prevRating = userStat.rating();
        log.info("User {} (ID: {}) previous rating: {}", userStat.userAccountEntity().nickname(),
            userId, prevRating);

        if (submission.status() == StatusType.ACCEPTED) {
            userStat.incrementSolvedProblemsCount();
            RatingResponseDto rating = userRatingUtil.calculateUserRating(
                getUserTopProblemRatings(userId),
                userStat.submissionCount(),
                userStat.solvedCount(),
                userStat.contributionCount()
            );

            userStat.setRating(rating.totalRating());
            userStat.setTopProblemRating(rating.topProblemRating());
            // Category Rating은 batch에서 처리

            log.info("User {} (ID: {}) new rating: {}",
                userStat.userAccountEntity().nickname(),
                userId, rating.totalRating());
        }

        return SubmissionReviewResponseDto.builder()
            .videoId(submission.videoId())
            .status(submission.status())
            .reason(submission.statusReason())
            .build();
    }

    private List<Integer> getUserTopProblemRatings(Long userId) {
        return submissionRepository.getUserTopProblems(
                userId,
                StatusType.ACCEPTED,
                Pageable.ofSize(50)
            ).stream()
            .map(ProblemInfoResponseDto::rating)
            .toList();
    }
}
