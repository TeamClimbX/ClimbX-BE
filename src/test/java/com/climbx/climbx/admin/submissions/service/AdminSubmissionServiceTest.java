package com.climbx.climbx.admin.submissions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import com.climbx.climbx.admin.submission.dto.SubmissionReviewRequestDto;
import com.climbx.climbx.admin.submission.dto.SubmissionReviewResponseDto;
import com.climbx.climbx.admin.submission.exception.StatusModifyToPendingException;
import com.climbx.climbx.admin.submission.service.AdminSubmissionService;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.submission.exception.PendingSubmissionNotFoundException;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.RatingResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.util.UserRatingUtil;
import com.climbx.climbx.video.entity.VideoEntity;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminSubmissionService 테스트")
class AdminSubmissionServiceTest {

    @InjectMocks
    private AdminSubmissionService adminSubmissionService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserStatRepository userStatRepository;

    @Nested
    @DisplayName("reviewSubmission 메서드 테스트")
    class ReviewSubmissionTest {

        @Test
        @DisplayName("유효한 PENDING 상태의 제출을 ACCEPTED로 승인하면 성공한다")
        void givenValidPendingSubmission_whenReviewWithAccepted_thenSuccess() {
            // Given
            UUID videoId = UUID.randomUUID();
            Long userId = 1L;
            String reason = "승인 완료";
            int oldRating = 1200;
            int newRating = 1250;

            SubmissionReviewRequestDto request = SubmissionReviewRequestDto.builder()
                .status(StatusType.ACCEPTED)
                .reason(reason)
                .build();

            VideoEntity videoEntity = VideoEntity.builder()
                .videoId(videoId)
                .userId(userId)
                .build();

            SubmissionEntity submission = SubmissionEntity.builder()
                .videoId(videoId)
                .status(StatusType.PENDING)
                .videoEntity(videoEntity)
                .build();

            UserAccountEntity userAccount = UserAccountEntity.builder()
                .userId(userId)
                .nickname("testUser")
                .build();

            UserStatEntity userStat = UserStatEntity.builder()
                .userId(userId)
                .rating(oldRating)
                .submissionCount(10)
                .solvedCount(5)
                .contributionCount(3)
                .userAccountEntity(userAccount)
                .build();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));
            given(userStatRepository.findById(userId))
                .willReturn(Optional.of(userStat));

            // When
            try (MockedStatic<UserRatingUtil> mockedStatic = mockStatic(UserRatingUtil.class)) {
                // Service increments solvedCount BEFORE rating calculation (5 -> 6)
                mockedStatic.when(() -> UserRatingUtil.calculateUserRating(
                        userStat.topProblemRating(), // default 0
                        userStat.submissionCount(),  // 10
                        userStat.solvedCount() + 1,  // will become 6 after increment
                        userStat.contributionCount() // 3
                    ))
                    .thenReturn(RatingResponseDto.builder()
                        .totalRating(newRating)
                        .topProblemRating(0)
                        .submissionRating(0)
                        .solvedRating(0)
                        .contributionRating(0)
                        .build());

                SubmissionReviewResponseDto result = adminSubmissionService.reviewSubmission(
                    videoId, request);

                // Then
                assertThat(result.videoId()).isEqualTo(videoId);
                assertThat(result.status()).isEqualTo(StatusType.ACCEPTED);
                assertThat(result.reason()).isEqualTo(reason);
                assertThat(userStat.rating()).isEqualTo(newRating);
                assertThat(userStat.solvedCount()).isEqualTo(6); // 5 + 1 increment

                then(submissionRepository).should(times(1)).findById(videoId);
                then(userStatRepository).should(times(1)).findById(userId);

                mockedStatic.verify(() -> UserRatingUtil.calculateUserRating(0, 10, 6, 3),
                    times(1));
            }
        }

        @Test
        @DisplayName("ACCEPTED 승인 시 사용자 통계를 찾을 수 없으면 UserNotFoundException이 발생한다")
        void givenAcceptedStatusButUserNotFound_whenReview_thenThrowUserNotFoundException() {
            // Given
            UUID videoId = UUID.randomUUID();
            Long userId = 1L;
            String reason = "승인 완료";

            SubmissionReviewRequestDto request = SubmissionReviewRequestDto.builder()
                .status(StatusType.ACCEPTED)
                .reason(reason)
                .build();

            VideoEntity videoEntity = VideoEntity.builder()
                .videoId(videoId)
                .userId(userId)
                .build();

            SubmissionEntity submission = SubmissionEntity.builder()
                .videoId(videoId)
                .status(StatusType.PENDING)
                .videoEntity(videoEntity)
                .build();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));
            given(userStatRepository.findById(userId))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminSubmissionService.reviewSubmission(videoId, request))
                .isInstanceOf(UserNotFoundException.class);

            then(submissionRepository).should(times(1)).findById(videoId);
            then(userStatRepository).should(times(1)).findById(userId);
        }

        @Test
        @DisplayName("유효한 PENDING 상태의 제출을 REJECTED로 거절하면 성공한다")
        void givenValidPendingSubmission_whenReviewWithRejected_thenSuccess() {
            // Given
            UUID videoId = UUID.randomUUID();
            Long userId = 1L;
            String reason = "부적절한 내용";

            SubmissionReviewRequestDto request = SubmissionReviewRequestDto.builder()
                .status(StatusType.REJECTED)
                .reason(reason)
                .build();

            VideoEntity videoEntity = VideoEntity.builder()
                .videoId(videoId)
                .userId(userId)
                .build();

            SubmissionEntity submission = SubmissionEntity.builder()
                .videoId(videoId)
                .status(StatusType.PENDING)
                .videoEntity(videoEntity)
                .build();

            UserAccountEntity userAccount = UserAccountEntity.builder()
                .userId(userId)
                .nickname("testUser")
                .build();

            UserStatEntity userStat = UserStatEntity.builder()
                .userId(userId)
                .rating(1200)
                .submissionCount(10)
                .solvedCount(5)
                .contributionCount(3)
                .userAccountEntity(userAccount)
                .build();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));
            given(userStatRepository.findById(userId))
                .willReturn(Optional.of(userStat));

            // When & Then
            try (MockedStatic<UserRatingUtil> mockedStatic = mockStatic(UserRatingUtil.class)) {
                SubmissionReviewResponseDto result = adminSubmissionService.reviewSubmission(
                    videoId,
                    request);

                // Then
                assertThat(result.videoId()).isEqualTo(videoId);
                assertThat(result.status()).isEqualTo(StatusType.REJECTED);
                assertThat(result.reason()).isEqualTo(reason);
                // REJECTED의 경우 레이팅이 변경되지 않아야 함
                assertThat(userStat.rating()).isEqualTo(1200);
                assertThat(userStat.solvedCount()).isEqualTo(5); // 변경되지 않음

                then(submissionRepository).should(times(1)).findById(videoId);
                then(userStatRepository).should(times(1)).findById(userId);
                // REJECTED의 경우 레이팅 계산 관련 메서드는 호출되지 않아야 함
                then(submissionRepository).should(times(0))
                    .getUserTopProblems(userId, StatusType.ACCEPTED, Pageable.ofSize(50));

                // Static 메서드는 호출되지 않아야 함
                mockedStatic.verify(
                    () -> UserRatingUtil.calculateUserRating(anyInt(), anyInt(), anyInt(),
                        anyInt()), times(0));
            }
        }

        @Test
        @DisplayName("존재하지 않는 비디오 ID로 요청하면 PendingSubmissionNotFoundException이 발생한다")
        void givenNonExistentVideoId_whenReview_thenThrowPendingSubmissionNotFoundException() {
            // Given
            UUID videoId = UUID.randomUUID();
            SubmissionReviewRequestDto request = SubmissionReviewRequestDto.builder()
                .status(StatusType.ACCEPTED)
                .reason("승인")
                .build();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminSubmissionService.reviewSubmission(videoId, request))
                .isInstanceOf(PendingSubmissionNotFoundException.class);

            then(submissionRepository).should(times(1)).findById(videoId);
        }

        @Test
        @DisplayName("PENDING이 아닌 상태의 제출을 리뷰하면 PendingSubmissionNotFoundException이 발생한다")
        void givenNonPendingSubmission_whenReview_thenThrowPendingSubmissionNotFoundException() {
            // Given
            UUID videoId = UUID.randomUUID();
            SubmissionReviewRequestDto request = SubmissionReviewRequestDto.builder()
                .status(StatusType.ACCEPTED)
                .reason("승인")
                .build();

            SubmissionEntity submission = SubmissionEntity.builder()
                .videoId(videoId)
                .status(StatusType.ACCEPTED) // 이미 승인된 상태
                .build();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // When & Then
            assertThatThrownBy(() -> adminSubmissionService.reviewSubmission(videoId, request))
                .isInstanceOf(PendingSubmissionNotFoundException.class);

            then(submissionRepository).should(times(1)).findById(videoId);
        }

        @Test
        @DisplayName("REJECTED 상태의 제출을 리뷰하면 PendingSubmissionNotFoundException이 발생한다")
        void givenRejectedSubmission_whenReview_thenThrowPendingSubmissionNotFoundException() {
            // Given
            UUID videoId = UUID.randomUUID();
            SubmissionReviewRequestDto request = SubmissionReviewRequestDto.builder()
                .status(StatusType.ACCEPTED)
                .reason("승인")
                .build();

            SubmissionEntity submission = SubmissionEntity.builder()
                .videoId(videoId)
                .status(StatusType.REJECTED) // 이미 거절된 상태
                .build();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // When & Then
            assertThatThrownBy(() -> adminSubmissionService.reviewSubmission(videoId, request))
                .isInstanceOf(PendingSubmissionNotFoundException.class);

            then(submissionRepository).should(times(1)).findById(videoId);
        }

        @Test
        @DisplayName("검토 상태를 PENDING으로 변경하려고 하면 StatusModifyToPendingException이 발생한다")
        void givenPendingStatus_whenReview_thenThrowStatusModifyToPendingException() {
            // Given
            UUID videoId = UUID.randomUUID();
            SubmissionReviewRequestDto request = SubmissionReviewRequestDto.builder()
                .status(StatusType.PENDING)
                .reason("PENDING으로 변경 시도")
                .build();

            // When & Then
            assertThatThrownBy(() -> adminSubmissionService.reviewSubmission(videoId, request))
                .isInstanceOf(StatusModifyToPendingException.class);

            // Repository 호출이 없어야 함 (예외가 먼저 발생하므로)
            then(submissionRepository).should(times(0)).findById(videoId);
        }
    }
}
