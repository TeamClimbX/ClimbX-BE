package com.climbx.climbx.submission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.fixture.GymAreaFixture;
import com.climbx.climbx.fixture.GymFixture;
import com.climbx.climbx.fixture.ProblemFixture;
import com.climbx.climbx.fixture.UserFixture;
import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
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
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.video.entity.VideoEntity;
import com.climbx.climbx.video.exception.VideoNotFoundException;
import com.climbx.climbx.video.repository.VideoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmissionService 테스트")
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private SubmissionService submissionService;

    // 테스트 헬퍼 메서드들
    private SubmissionEntity createSubmission() {
        return createSubmission(createVideo(1L, UUID.randomUUID()),
            createProblem(UUID.randomUUID()));
    }

    private SubmissionEntity createSubmission(VideoEntity video, ProblemEntity problem) {
        return SubmissionEntity.builder()
            .videoId(video.videoId())
            .videoEntity(video)
            .problemEntity(problem)
            .status(StatusType.PENDING)
            .build();
    }

    private VideoEntity createVideo(Long userId, UUID videoId) {
        UserAccountEntity user = UserFixture.createUser(userId);
        return VideoEntity.builder()
            .videoId(videoId)
            .userId(userId)
            .userAccountEntity(user)
            .fileSize(1024L)
            .status(StatusType.COMPLETED)
            .build();
    }

    private UserAccountEntity createUser(Long userId, String nickname) {
        return UserFixture.createUserAccountEntity(userId, nickname);
    }

    private ProblemEntity createProblem(UUID problemId) {
        GymEntity gym = GymFixture.createGymEntity(1L, "테스트 클라이밍 센터", 37.5665, 126.9780);
        GymAreaEntity gymArea = GymAreaFixture.createGymAreaEntity(1L, gym, "테스트 구역");
        return ProblemFixture.createProblemEntity(problemId, gym, gymArea);
    }

    @Nested
    @DisplayName("제출 목록 조회 테스트")
    class GetSubmissionsTest {

        @Test
        @DisplayName("제출 목록을 성공적으로 조회한다")
        void shouldGetSubmissionsSuccessfully() {
            // given
            String nickname = "testUser";
            Long userId = 1L;
            UserAccountEntity user = createUser(userId, nickname);
            SubmissionEntity submission = createSubmission();
            Page<SubmissionEntity> submissionPage = new PageImpl<>(List.of(submission));
            Pageable pageable = PageRequest.of(0, 10);

            given(userAccountRepository.findByNickname(nickname))
                .willReturn(Optional.of(user));
            given(submissionRepository.findSubmissionsWithFilters(
                eq(userId), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(submissionPage);

            // when
            SubmissionListResponseDto result = submissionService.getSubmissions(
                nickname, null, null, null, null, pageable
            );

            // then
            assertThat(result.submissions()).hasSize(1);
            assertThat(result.totalCount()).isEqualTo(1L);
            assertThat(result.hasNext()).isFalse();

            then(userAccountRepository).should().findByNickname(nickname);
            then(submissionRepository).should().findSubmissionsWithFilters(
                eq(userId), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            );
        }
    }

    @Nested
    @DisplayName("제출 생성 테스트")
    class CreateSubmissionTest {

        @Test
        @DisplayName("유효한 요청으로 제출을 성공적으로 생성한다")
        void shouldCreateSubmissionSuccessfully() {
            // given
            String nickname = "testUser";
            Long userId = 1L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionCreateRequestDto request = new SubmissionCreateRequestDto(videoId, problemId);

            UserAccountEntity user = createUser(userId, nickname);
            VideoEntity video = createVideo(userId, videoId);
            ProblemEntity problem = createProblem(problemId);

            given(userAccountRepository.findById(userId))
                .willReturn(Optional.of(user));
            given(videoRepository.findByVideoIdAndStatus(videoId, StatusType.COMPLETED))
                .willReturn(Optional.of(video));
            given(submissionRepository.findById(videoId))
                .willReturn(Optional.empty());
            given(problemRepository.findById(problemId))
                .willReturn(Optional.of(problem));
            given(submissionRepository.save(any(SubmissionEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            SubmissionResponseDto result = submissionService.createSubmission(userId, request);

            // then
            assertThat(result.status()).isEqualTo(StatusType.PENDING);

            then(videoRepository).should().findByVideoIdAndStatus(videoId, StatusType.COMPLETED);
            then(problemRepository).should().findById(problemId);
            then(submissionRepository).should().save(any(SubmissionEntity.class));
        }

        @Test
        @DisplayName("존재하지 않는 비디오로 제출 생성 시 예외를 던진다")
        void shouldThrowExceptionWhenVideoNotFound() {
            // given
            Long userId = 1L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionCreateRequestDto request = new SubmissionCreateRequestDto(videoId, problemId);

            given(videoRepository.findByVideoIdAndStatus(videoId, StatusType.COMPLETED))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> submissionService.createSubmission(userId, request))
                .isInstanceOf(VideoNotFoundException.class);

            then(videoRepository).should().findByVideoIdAndStatus(videoId, StatusType.COMPLETED);
        }

        @Test
        @DisplayName("다른 사용자의 비디오로 제출 생성 시 예외를 던진다")
        void shouldThrowExceptionWhenVideoOwnerMismatch() {
            // given
            Long userId = 1L;
            Long otherUserId = 2L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionCreateRequestDto request = new SubmissionCreateRequestDto(videoId, problemId);

            VideoEntity video = createVideo(otherUserId, videoId);

            given(videoRepository.findByVideoIdAndStatus(videoId, StatusType.COMPLETED))
                .willReturn(Optional.of(video));

            // when & then
            assertThatThrownBy(() -> submissionService.createSubmission(userId, request))
                .isInstanceOf(ForbiddenSubmissionException.class);

            then(videoRepository).should().findByVideoIdAndStatus(videoId, StatusType.COMPLETED);
        }

        @Test
        @DisplayName("존재하지 않는 문제로 제출 생성 시 예외를 던진다")
        void shouldThrowExceptionWhenProblemNotFound() {
            // given
            Long userId = 1L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionCreateRequestDto request = new SubmissionCreateRequestDto(videoId, problemId);

            VideoEntity video = createVideo(userId, videoId);

            given(videoRepository.findByVideoIdAndStatus(videoId, StatusType.COMPLETED))
                .willReturn(Optional.of(video));
            given(submissionRepository.findById(videoId))
                .willReturn(Optional.empty());
            given(problemRepository.findById(problemId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> submissionService.createSubmission(userId, request))
                .isInstanceOf(ProblemNotFoundException.class);

            then(problemRepository).should().findById(problemId);
        }

        @Test
        @DisplayName("이미 제출된 영상으로 제출 생성 시 예외를 던진다")
        void shouldThrowExceptionWhenVideoAlreadySubmitted() {
            // given
            Long userId = 1L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionCreateRequestDto request = new SubmissionCreateRequestDto(videoId, problemId);

            VideoEntity video = createVideo(userId, videoId);
            SubmissionEntity existingSubmission = createSubmission(video, createProblem(problemId));

            given(videoRepository.findByVideoIdAndStatus(videoId, StatusType.COMPLETED))
                .willReturn(Optional.of(video));
            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(existingSubmission));

            // when & then
            assertThatThrownBy(() -> submissionService.createSubmission(userId, request))
                .isInstanceOf(DuplicateSubmissionException.class);

            then(submissionRepository).should().findById(videoId);
        }
    }

    @Nested
    @DisplayName("제출 조회 테스트")
    class GetSubmissionByVideoIdTest {

        @Test
        @DisplayName("비디오 ID로 제출을 성공적으로 조회한다")
        void shouldGetSubmissionByVideoIdSuccessfully() {
            // given
            UUID videoId = UUID.randomUUID();
            SubmissionEntity submission = createSubmission();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // when
            SubmissionResponseDto result = submissionService.getSubmissionByVideoId(videoId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(StatusType.PENDING);

            then(submissionRepository).should().findById(videoId);
        }

        @Test
        @DisplayName("존재하지 않는 제출 조회 시 예외를 던진다")
        void shouldThrowExceptionWhenSubmissionNotFound() {
            // given
            UUID videoId = UUID.randomUUID();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> submissionService.getSubmissionByVideoId(videoId))
                .isInstanceOf(VideoNotFoundException.class);

            then(submissionRepository).should().findById(videoId);
        }
    }

    @Nested
    @DisplayName("제출 취소 테스트")
    class CancelSubmissionTest {

        @Test
        @DisplayName("제출을 성공적으로 취소한다")
        void shouldCancelSubmissionSuccessfully() {
            // given
            Long userId = 1L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            VideoEntity video = createVideo(userId, videoId);
            SubmissionEntity submission = createSubmission(video, createProblem(problemId));

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // when
            SubmissionCancelResponseDto result = submissionService.cancelSubmission(userId,
                videoId);

            // then
            assertThat(result).isNotNull();

            then(submissionRepository).should().findById(videoId);
        }

        @Test
        @DisplayName("다른 사용자의 제출 취소 시 예외를 던진다")
        void shouldThrowExceptionWhenCancelOthersSubmission() {
            // given
            Long userId = 1L;
            Long otherUserId = 2L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            VideoEntity video = createVideo(otherUserId, videoId);
            SubmissionEntity submission = createSubmission(video, createProblem(problemId));

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // when & then
            assertThatThrownBy(() -> submissionService.cancelSubmission(userId, videoId))
                .isInstanceOf(ForbiddenSubmissionException.class);

            then(submissionRepository).should().findById(videoId);
        }
    }

    @Nested
    @DisplayName("제출 이의제기 테스트")
    class AppealSubmissionTest {

        @Test
        @DisplayName("제출 이의제기를 성공적으로 처리한다")
        void shouldAppealSubmissionSuccessfully() {
            // given
            Long userId = 1L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionAppealRequestDto request = SubmissionAppealRequestDto.builder()
                .reason("정당한 이의제기 사유")
                .build();
            VideoEntity video = createVideo(userId, videoId);
            SubmissionEntity submission = createSubmission(video, createProblem(problemId));

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // when
            SubmissionAppealResponseDto result = submissionService.appealSubmission(userId,
                videoId,
                request);

            // then
            assertThat(result).isNotNull();

            then(submissionRepository).should().findById(videoId);
        }

        @Test
        @DisplayName("다른 사용자의 제출에 이의제기 시 예외를 던진다")
        void shouldThrowExceptionWhenAppealOthersSubmission() {
            // given
            Long userId = 1L;
            Long otherUserId = 2L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionAppealRequestDto request = SubmissionAppealRequestDto.builder()
                .reason("이의제기 사유")
                .build();
            VideoEntity video = createVideo(otherUserId, videoId);
            SubmissionEntity submission = createSubmission(video, createProblem(problemId));

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // when & then
            assertThatThrownBy(() -> submissionService.appealSubmission(userId, videoId, request))
                .isInstanceOf(ForbiddenSubmissionException.class);

            then(submissionRepository).should().findById(videoId);
        }

        @Test
        @DisplayName("동일한 내용으로 중복 이의제기 시 예외를 던진다")
        void shouldThrowExceptionWhenDuplicateAppeal() {
            // given
            Long userId = 1L;
            UUID videoId = UUID.randomUUID();
            UUID problemId = UUID.randomUUID();
            SubmissionAppealRequestDto request = SubmissionAppealRequestDto.builder()
                .reason("동일한 이의제기 사유")
                .build();
            VideoEntity video = createVideo(userId, videoId);
            SubmissionEntity submission = SubmissionEntity.builder()
                .videoId(videoId)
                .videoEntity(video)
                .problemEntity(createProblem(problemId))
                .status(StatusType.PENDING)
                .appealContent(request.reason()) // 이미 동일한 내용으로 이의제기 되어 있음
                .build();

            given(submissionRepository.findById(videoId))
                .willReturn(Optional.of(submission));

            // when & then
            assertThatThrownBy(() -> submissionService.appealSubmission(userId, videoId, request))
                .isInstanceOf(DuplicateAppealException.class);

            then(submissionRepository).should().findById(videoId);
        }
    }
} 