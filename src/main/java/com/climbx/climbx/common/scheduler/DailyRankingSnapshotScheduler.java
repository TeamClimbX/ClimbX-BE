package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * DailyRankingSnapshotScheduler는 매일 정해진 시간에 사용자 랭킹 통계를 히스토리 테이블에 스냅샷으로 저장하는 스케줄러입니다.
 * <p>
 * - snapshotDailyRanking: 매일 00:05에 모든 사용자의 rating, longestStreak, solvedCount를 히스토리에 저장
 * - 대용량 데이터 처리를 위해 페이징 기반 배치 처리를 사용하여 메모리 효율성을 보장합니다.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DailyRankingSnapshotScheduler {

    private final UserStatRepository userStatRepository;
    private final RankingSnapshotProcessor rankingSnapshotProcessor;
    
    @Value("${scheduler.ranking.batch-size:1000}")
    private int batchSize;

    @Scheduled(cron = "${scheduler.ranking.cron:0 5 0 * * *}", zone = "Asia/Seoul")
    public void snapshotDailyRanking() {
        LocalDate today = LocalDate.now();
        log.debug("Starting daily ranking snapshot for date: {} with batch size: {}", today, batchSize);
        
        int successCount = 0;
        int failureCount = 0;
        long totalUsers = 0;
        Page<UserStatEntity> userStatsPage;
        int pageNumber = 0;

        do {
            Pageable pageable = PageRequest.of(pageNumber++, batchSize);
            userStatsPage = userStatRepository.findAll(pageable);
            totalUsers += userStatsPage.getNumberOfElements();
            
            log.debug("Processing batch {} with {} users (total processed: {})", 
                pageNumber, userStatsPage.getNumberOfElements(), totalUsers);

            for (UserStatEntity userStat : userStatsPage.getContent()) {
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
        } while (userStatsPage.hasNext());

        log.info(
            "Daily ranking snapshot completed for date: {} - Total: {}, Success: {}, Failure: {}",
            today, totalUsers, successCount, failureCount);
    }
}
