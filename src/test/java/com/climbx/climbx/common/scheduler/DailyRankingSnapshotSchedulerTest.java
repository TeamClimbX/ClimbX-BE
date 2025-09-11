package com.climbx.climbx.common.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.climbx.climbx.fixture.UserFixture;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DailyRankingSnapshotSchedulerTest {

    @Mock
    private UserStatRepository userStatRepository;

    @Mock
    private RankingSnapshotProcessor rankingSnapshotProcessor;

    @InjectMocks
    private DailyRankingSnapshotScheduler dailyRankingSnapshotScheduler;

    @Nested
    @DisplayName("snapshotDailyRanking 메서드는")
    class SnapshotDailyRankingTest {

        @Test
        @DisplayName("모든 유저 통계를 3개 기준으로 히스토리에 저장한다")
        void shouldSaveAllUserStatsToHistoryWithThreeCriteria() {
            // given
            ReflectionTestUtils.setField(dailyRankingSnapshotScheduler, "batchSize", 1000);
            
            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L, 1500);
            UserStatEntity userStat2 = UserFixture.createUserStatEntity(2L, 1600);
            List<UserStatEntity> userStats = List.of(userStat1, userStat2);
            
            Page<UserStatEntity> page = new PageImpl<>(userStats);
            given(userStatRepository.findAll(any(Pageable.class))).willReturn(page);

            // when
            dailyRankingSnapshotScheduler.snapshotDailyRanking();

            // then
            then(rankingSnapshotProcessor).should(times(2))
                .createUserRankingSnapshotInNewTransaction(any(UserStatEntity.class));
        }

        @Test
        @DisplayName("유저가 없을 때 정상적으로 완료한다")
        void shouldCompleteNormallyWhenNoUsers() {
            // given
            ReflectionTestUtils.setField(dailyRankingSnapshotScheduler, "batchSize", 1000);
            
            Page<UserStatEntity> emptyPage = new PageImpl<>(Collections.emptyList());
            given(userStatRepository.findAll(any(Pageable.class))).willReturn(emptyPage);

            // when
            dailyRankingSnapshotScheduler.snapshotDailyRanking();

            // then
            then(rankingSnapshotProcessor).shouldHaveNoInteractions();
        }
    }
}