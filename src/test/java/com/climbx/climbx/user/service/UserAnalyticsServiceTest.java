package com.climbx.climbx.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.gym.enums.GymTierType;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.enums.HoldColorType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserAnalyticsServiceTest {

    @Mock
    private UserLookupService userLookupService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRankingHistoryRepository userRankingHistoryRepository;

    @InjectMocks
    private UserAnalyticsService userAnalyticsService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private UserAccountEntity createMockUserAccountEntity(Long userId, String nickname) {
        return UserAccountEntity.builder()
            .userId(userId)
            .nickname(nickname)
            .role(RoleType.USER)
            .build();
    }

    private ProblemInfoResponseDto createMockProblemInfoResponseDto(String gymName,
        Integer rating) {
        return ProblemInfoResponseDto.builder()
            .problemId(UUID.randomUUID())
            .gymId(1L)
            .gymName(gymName)
            .gymAreaId(1L)
            .gymAreaName("Test Area")
            .localLevel(GymTierType.RED)
            .holdColor(HoldColorType.BLUE)
            .tier(ProblemTierType.B3)
            .rating(rating)
            .problemImageCdnUrl("http://example.com/image.jpg")
            .activeStatus(ActiveStatusType.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("사용자 상위 문제 조회")
    class GetUserTopProblems {

        @Test
        @DisplayName("사용자의 상위 문제를 정상 조회")
        void getUserTopProblems_Success() {
            // given
            String nickname = "alice";
            Integer limit = 5;
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            List<ProblemInfoResponseDto> expectedProblems = List.of(
                createMockProblemInfoResponseDto("Problem 1", 1500),
                createMockProblemInfoResponseDto("Problem 2", 1400)
            );

            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(submissionRepository.getUserTopProblems(eq(userId), eq(StatusType.ACCEPTED),
                any(Pageable.class)))
                .willReturn(expectedProblems);

            // when
            List<ProblemInfoResponseDto> result = userAnalyticsService.getUserTopProblems(nickname,
                limit);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).gymName()).isEqualTo("Problem 1");
            assertThat(result.get(1).gymName()).isEqualTo("Problem 2");

            then(userLookupService).should().findUserByNickname(nickname);
            then(submissionRepository).should()
                .getUserTopProblems(eq(userId), eq(StatusType.ACCEPTED), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageSize()).isEqualTo(limit);
            assertThat(pageable.getPageNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 닉네임으로 조회 시 예외 발생")
        void getUserTopProblems_UserNotFound() {
            // given
            String nickname = "nonexistent";
            Integer limit = 5;

            given(userLookupService.findUserByNickname(nickname))
                .willThrow(new UserNotFoundException(nickname));

            // when & then
            assertThatThrownBy(() -> userAnalyticsService.getUserTopProblems(nickname, limit))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("limit이 1 이상인 경우 정상 처리")
        void getUserTopProblems_ValidLimit() {
            // given
            String nickname = "alice";
            Integer limit = 1;
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(submissionRepository.getUserTopProblems(eq(userId), eq(StatusType.ACCEPTED),
                any(Pageable.class)))
                .willReturn(List.of());

            // when
            List<ProblemInfoResponseDto> result = userAnalyticsService.getUserTopProblems(nickname,
                limit);

            // then
            assertThat(result).isEmpty();
            then(submissionRepository).should()
                .getUserTopProblems(eq(userId), eq(StatusType.ACCEPTED), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageSize()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("사용자 스트릭 조회")
    class GetUserStreak {

        @Test
        @DisplayName("사용자의 일별 해결 문제 수를 정상 조회")
        void getUserStreak_Success() {
            // given
            String nickname = "alice";
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 7);
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            List<DailyHistoryResponseDto> expectedHistory = List.of(
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 3L),
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 2L),
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 3), 5L)
            );

            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(
                submissionRepository.getUserDateSolvedCount(userId, StatusType.ACCEPTED, from, to))
                .willReturn(expectedHistory);

            // when
            List<DailyHistoryResponseDto> result = userAnalyticsService.getUserStreak(nickname,
                from, to);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(result.get(0).value()).isEqualTo(3L);
            assertThat(result.get(1).value()).isEqualTo(2L);
            assertThat(result.get(2).value()).isEqualTo(5L);

            then(userLookupService).should().findUserByNickname(nickname);
            then(submissionRepository).should()
                .getUserDateSolvedCount(userId, StatusType.ACCEPTED, from, to);
        }

        @Test
        @DisplayName("해당 기간에 해결한 문제가 없는 경우")
        void getUserStreak_NoProblems() {
            // given
            String nickname = "alice";
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 7);
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(
                submissionRepository.getUserDateSolvedCount(userId, StatusType.ACCEPTED, from, to))
                .willReturn(List.of());

            // when
            List<DailyHistoryResponseDto> result = userAnalyticsService.getUserStreak(nickname,
                from, to);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("하루만 조회하는 경우")
        void getUserStreak_SingleDay() {
            // given
            String nickname = "alice";
            LocalDate date = LocalDate.of(2024, 1, 1);
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            List<DailyHistoryResponseDto> expectedHistory = List.of(
                new DailyHistoryResponseDto(date, 3L)
            );

            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(submissionRepository.getUserDateSolvedCount(userId, StatusType.ACCEPTED, date,
                date))
                .willReturn(expectedHistory);

            // when
            List<DailyHistoryResponseDto> result = userAnalyticsService.getUserStreak(nickname,
                date, date);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).date()).isEqualTo(date);
            assertThat(result.get(0).value()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("사용자 일별 히스토리 조회")
    class GetUserDailyHistory {

        @Test
        @DisplayName("사용자의 레이팅 히스토리를 정상 조회")
        void getUserDailyHistory_Success() {
            // given
            String nickname = "alice";
            CriteriaType criteria = CriteriaType.RATING;
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 7);
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            List<DailyHistoryResponseDto> expectedHistory = List.of(
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1200L),
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 1250L),
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 3), 1300L)
            );

            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                .willReturn(expectedHistory);

            // when
            List<DailyHistoryResponseDto> result = userAnalyticsService.getUserDailyHistory(
                nickname, criteria, from, to
            );

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).value()).isEqualTo(1200L);
            assertThat(result.get(1).value()).isEqualTo(1250L);
            assertThat(result.get(2).value()).isEqualTo(1300L);

            then(userLookupService).should().findUserByNickname(nickname);
            then(userRankingHistoryRepository).should()
                .getUserDailyHistory(userId, criteria, from, to);
        }

        @Test
        @DisplayName("다양한 criteria 타입으로 조회")
        void getUserDailyHistory_DifferentCriteria() {
            // given
            String nickname = "alice";
            CriteriaType criteria = CriteriaType.SOLVED_COUNT;
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 7);
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            List<DailyHistoryResponseDto> expectedHistory = List.of(
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 10L),
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 12L),
                new DailyHistoryResponseDto(LocalDate.of(2024, 1, 3), 15L)
            );

            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                .willReturn(expectedHistory);

            // when
            List<DailyHistoryResponseDto> result = userAnalyticsService.getUserDailyHistory(
                nickname, criteria, from, to
            );

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).value()).isEqualTo(10L);
            assertThat(result.get(1).value()).isEqualTo(12L);
            assertThat(result.get(2).value()).isEqualTo(15L);

            then(userRankingHistoryRepository).should()
                .getUserDailyHistory(userId, criteria, from, to);
        }

        @Test
        @DisplayName("해당 기간에 히스토리가 없는 경우")
        void getUserDailyHistory_NoHistory() {
            // given
            String nickname = "alice";
            CriteriaType criteria = CriteriaType.RATING;
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 7);
            Long userId = 1L;

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                .willReturn(List.of());

            // when
            List<DailyHistoryResponseDto> result = userAnalyticsService.getUserDailyHistory(
                nickname, criteria, from, to
            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 닉네임으로 히스토리 조회")
        void getUserDailyHistory_UserNotFound() {
            // given
            String nickname = "nonexistent";
            CriteriaType criteria = CriteriaType.RATING;
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 7);

            given(userLookupService.findUserByNickname(nickname))
                .willThrow(new UserNotFoundException(nickname));

            // when & then
            assertThatThrownBy(() -> userAnalyticsService.getUserDailyHistory(
                nickname, criteria, from, to
            )).isInstanceOf(UserNotFoundException.class);
        }
    }
}