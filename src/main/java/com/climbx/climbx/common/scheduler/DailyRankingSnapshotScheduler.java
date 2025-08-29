package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.common.enums.WorkItemType;
import com.climbx.climbx.common.service.WorkItemService;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.time.LocalDate;
import java.util.List;
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
    private final WorkItemService workItemService;

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
                .userId(us.userId())
                .userAccountEntity(us.userAccountEntity())
                .value(us.rating())
                .build());

            userRankingHistoryRepository.save(UserRankingHistoryEntity.builder()
                .criteria(CriteriaType.LONGEST_STREAK)
                .userId(us.userId())
                .userAccountEntity(us.userAccountEntity())
                .value(us.longestStreak())
                .build());

            userRankingHistoryRepository.save(UserRankingHistoryEntity.builder()
                .criteria(CriteriaType.SOLVED_COUNT)
                .userId(us.userId())
                .userAccountEntity(us.userAccountEntity())
                .value(us.solvedCount())
                .build());
        });

        // 스냅샷 완료 마커 work item (idempotent)
        workItemService.enqueueUnique(WorkItemType.RANKING_HISTORY_SNAPSHOT, today.toString(), "{}");
    }
}


