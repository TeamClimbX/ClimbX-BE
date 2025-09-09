package com.climbx.climbx.admin.submission.service;

import com.climbx.climbx.admin.submission.dto.SubmissionReviewRequestDto;
import com.climbx.climbx.admin.submission.dto.SubmissionReviewResponseDto;
import com.climbx.climbx.admin.submission.exception.StatusModifyToPendingException;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.repository.ContributionRepository;
import com.climbx.climbx.problem.service.ProblemService;
import com.climbx.climbx.common.service.OutboxService;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.submission.exception.PendingSubmissionNotFoundException;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.RatingResponseDto;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.service.UserDataAggregationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AdminSubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserStatRepository userStatRepository;
    private final UserDataAggregationService userDataAggregationService;
    private final OutboxService outboxService;
    private final ContributionRepository contributionRepository;
    private final ProblemService problemService;

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
        UUID problemId = submission.problemEntity().problemId();

        UserStatEntity userStat = userStatRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        int prevRating = userStat.rating();
        log.info("User {} (ID: {}) previous rating: {}", userStat.userAccountEntity().nickname(),
            userId, prevRating);

        if (submission.status() == StatusType.ACCEPTED) {
            userStat.incrementSolvedProblemsCount();
            
            // topProblemRating 갱신 후 전체 레이팅 재계산
            userDataAggregationService.recalculateAndUpdateUserRating(userId);
            
            log.info("User {} (ID: {}) rating updated after submission approval",
                userStat.userAccountEntity().nickname(), userId);
            contributionRepository.findByUserIdAndProblemId(
                userId,
                problemId
            ).ifPresent(
                c -> {
                    problemService.applyVoteToProblem(
                        submission.problemEntity(),
                        c,
                        c.contributionTags()
                    );
                }
            );

            // 로깅을 위해 현재 rating 조회 (optional)
            try {
                RatingResponseDto rating = userDataAggregationService.calculateUserRatingFromCurrentStats(userStat);
                if (rating != null) {
                    log.info("User {} (ID: {}) new rating: {}",
                        userStat.userAccountEntity().nickname(),
                        userId, rating.totalRating());
                }
            } catch (Exception e) {
                log.debug("Failed to calculate rating for logging: {}", e.getMessage());
            }

            try {
                outboxService.recordEvent(
                    "user",
                    userId.toString(),
                    OutboxEventType.USER_SOLVED_PROBLEM
                );
            } catch (Exception ignored) {
            }
        }

        return SubmissionReviewResponseDto.builder()
            .videoId(submission.videoId())
            .status(submission.status())
            .reason(submission.statusReason())
            .build();
    }
}
