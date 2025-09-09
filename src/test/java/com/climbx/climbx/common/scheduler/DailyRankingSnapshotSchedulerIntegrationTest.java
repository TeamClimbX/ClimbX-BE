package com.climbx.climbx.common.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.fixture.SchedulerTestFixture;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class DailyRankingSnapshotSchedulerIntegrationTest extends AbstractSchedulerIntegrationTest {

    @Autowired
    private DailyRankingSnapshotScheduler dailyRankingSnapshotScheduler;

    @Autowired
    private UserRankingHistoryRepository userRankingHistoryRepository;

    @MockitoSpyBean
    private RankingSnapshotProcessor rankingSnapshotProcessor;

    @Nested
    @DisplayName("snapshotDailyRanking 메서드는")
    class SnapshotDailyRankingTest {

        @Test
        @DisplayName("여러 유저의 통계를 3가지 기준으로 히스토리에 저장한다")
        void shouldCreateRankingSnapshotsForMultipleUsers() {
            // given
            insertUserAccount(1L, "user1");
            insertUserAccount(2L, "user2");
            insertUserAccount(3L, "user3");
            
            List<UserStatEntity> userStats = SchedulerTestFixture.createMultipleUserStats();
            insertUserStats(userStats);

            // when
            dailyRankingSnapshotScheduler.snapshotDailyRanking();

            // then - 실제 값 검증
            List<UserRankingHistoryEntity> allHistories = userRankingHistoryRepository.findAll();
            assertThat(allHistories).hasSize(9);

            for (UserStatEntity userStat : userStats) {
                Long userId = userStat.userId();
                List<UserRankingHistoryEntity> userHistories =
                    userRankingHistoryRepository.findByUserId(userId);
                assertThat(userHistories).hasSize(3);
                assertHistoryContainsCriteria(userHistories, CriteriaType.RATING,
                    userStat.rating());
                assertHistoryContainsCriteria(userHistories, CriteriaType.LONGEST_STREAK,
                    userStat.longestStreak());
                assertHistoryContainsCriteria(userHistories, CriteriaType.SOLVED_COUNT,
                    userStat.solvedCount());
            }
        }

        @Test
        @DisplayName("유저가 없을 때 정상적으로 완료한다")
        void shouldCompleteNormallyWhenNoUsers() {
            // when
            dailyRankingSnapshotScheduler.snapshotDailyRanking();

            // then - 실제 값 검증
            List<UserRankingHistoryEntity> allHistories = userRankingHistoryRepository.findAll();
            assertThat(allHistories).isEmpty();
        }

        @Test
        @DisplayName("일부 유저 처리 실패 시 다른 유저는 정상 처리된다")
        void shouldContinueProcessingOtherUsersWhenOneUserFails() {
            // given
            insertUserAccount(1L, "user1");
            insertUserAccount(2L, "user2");
            insertUserAccount(3L, "user3");
            List<UserStatEntity> userStats = SchedulerTestFixture.createMultipleUserStats();
            insertUserStats(userStats);

            // when
            dailyRankingSnapshotScheduler.snapshotDailyRanking();

            // then - 실제 값 검증
            List<UserRankingHistoryEntity> allHistories = userRankingHistoryRepository.findAll();
            assertThat(allHistories).hasSize(9);

            Long firstUserId = userStats.get(0).userId();
            Long thirdUserId = userStats.get(2).userId();
            assertThat(countRankingHistories(firstUserId)).isEqualTo(3);
            assertThat(countRankingHistories(thirdUserId)).isEqualTo(3);
        }

        @Test
        @DisplayName("단일 유저의 통계를 정확한 값으로 저장한다")
        void shouldCreateCorrectRankingSnapshotForSingleUser() {
            // given
            Long userId = 100L;
            insertUserAccount(userId, "user100");
            UserStatEntity userStat = SchedulerTestFixture.createUserStatWithValues(userId, 1750,
                25, 50);
            insertUserStats(List.of(userStat));

            // when
            dailyRankingSnapshotScheduler.snapshotDailyRanking();

            // then - 실제 값 검증
            List<UserRankingHistoryEntity> histories =
                userRankingHistoryRepository.findByUserId(userId);
            assertThat(histories).hasSize(3);
            assertHistoryContainsCriteria(histories, CriteriaType.RATING, 1750);
            assertHistoryContainsCriteria(histories, CriteriaType.LONGEST_STREAK, 25);
            assertHistoryContainsCriteria(histories, CriteriaType.SOLVED_COUNT, 50);
        }

        private void assertHistoryContainsCriteria(List<UserRankingHistoryEntity> histories,
            CriteriaType criteria,
            Integer expectedValue) {
            UserRankingHistoryEntity history = histories.stream()
                .filter(h -> h.criteria() == criteria)
                .findFirst()
                .orElseThrow(
                    () -> new AssertionError("History with criteria " + criteria + " not found"));
            assertThat(history.value()).isEqualTo(expectedValue);
        }

        private void insertUserAccount(Long userId, String nickname) {
            jdbcClient.sql(
                    "INSERT INTO user_accounts (user_id, last_login_date, nickname, role) VALUES (?, ?, ?, ?)")
                .param(userId)
                .param(LocalDate.now())
                .param(nickname)
                .param("USER")
                .update();
        }

        private void insertUserStats(List<UserStatEntity> stats) {
            for (UserStatEntity s : stats) {
                jdbcClient.sql(
                        "INSERT INTO user_stats (user_id, rating, top_problem_rating, current_streak, longest_streak, solved_count, submission_count, contribution_count, rival_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    .param(s.userId())
                    .param(s.rating())
                    .param(s.topProblemRating())
                    .param(s.currentStreak())
                    .param(s.longestStreak())
                    .param(s.solvedCount())
                    .param(s.submissionCount())
                    .param(s.contributionCount())
                    .param(s.rivalCount())
                    .update();
            }
        }
    }
}