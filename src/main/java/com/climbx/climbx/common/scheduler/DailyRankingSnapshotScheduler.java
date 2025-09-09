package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * DailyRankingSnapshotScheduler는 매일 정해진 시간에 사용자 랭킹 통계를 히스토리 테이블에 스냅샷으로 저장하는 스케줄러입니다.
 * <p>
 * - snapshotDailyRanking: 매일 00:05에 모든 사용자의 rating, longestStreak, solvedCount를 히스토리에 저장
 * <p>
 * TODO: 대용량 데이터 처리 시 배치 처리 성능 최적화 및 메모리 효율성 개선 필요
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DailyRankingSnapshotScheduler {

    private final UserStatRepository userStatRepository;
    private final RankingSnapshotProcessor rankingSnapshotProcessor;

    @Scheduled(cron = "${scheduler.ranking.cron:0 5 0 * * *}", zone = "Asia/Seoul")
    public void snapshotDailyRanking() {
        LocalDate today = LocalDate.now();
        List<UserStatEntity> userStats = userStatRepository.findAll();
        log.debug("Starting daily ranking snapshot for date: {} - Total users: {}", today,
            userStats.size());
        int successCount = 0;
        int failureCount = 0;

        for (UserStatEntity userStat : userStats) {
            try {
                log.debug("Processing ranking snapshot for userId: {}", userStat.userId());
                rankingSnapshotProcessor.createUserRankingSnapshotInNewTransaction(userStat);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to create ranking snapshot for userId: {}, error: {}",
                    userStat.userId(), e.getMessage(), e);
            }
        }

        log.info(
            "Daily ranking snapshot completed for date: {} - Total: {}, Success: {}, Failure: {}",
            today, userStats.size(), successCount, failureCount);
    }
}
