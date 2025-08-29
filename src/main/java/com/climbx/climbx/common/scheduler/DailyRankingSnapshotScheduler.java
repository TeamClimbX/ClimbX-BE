package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DailyRankingSnapshotScheduler {

    private final UserStatRepository userStatRepository;
    private final UserRankingHistoryRepository userRankingHistoryRepository;

    // 00:05 UTC에 스냅샷 작업 실행
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void snapshotDailyRanking() {
        LocalDate today = LocalDate.now();
        var userStats = userStatRepository.findAll();

        // criteria 별로 history insert
        userStats.forEach(us -> {
            userRankingHistoryRepository.save(UserRankingHistoryEntity.builder()
                .criteria(CriteriaType.RATING)
                .userAccountEntity(us.userAccountEntity())
                .value(us.rating())
                .build());

            userRankingHistoryRepository.save(UserRankingHistoryEntity.builder()
                .criteria(CriteriaType.LONGEST_STREAK)
                .userAccountEntity(us.userAccountEntity())
                .value(us.longestStreak())
                .build());

            userRankingHistoryRepository.save(UserRankingHistoryEntity.builder()
                .criteria(CriteriaType.SOLVED_COUNT)
                .userAccountEntity(us.userAccountEntity())
                .value(us.solvedCount())
                .build());
        });

        // 스냅샷 완료 로그
        log.info("Daily ranking snapshot completed for {} users on date: {}", userStats.size(),
            today);
    }
}
