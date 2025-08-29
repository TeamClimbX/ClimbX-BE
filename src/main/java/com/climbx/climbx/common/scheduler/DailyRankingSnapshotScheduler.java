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

/**
 * DailyRankingSnapshotScheduler는 매일 정해진 시간에 사용자 랭킹 통계를 히스토리 테이블에 스냅샷으로 저장하는 스케줄러입니다.
 *
 * - snapshotDailyRanking: 매일 00:05에 모든 사용자의 rating, longestStreak, solvedCount를 히스토리에 저장
 *
 * TODO: 인테그레이션 테스트 추가 필요 - 실제 DB와 함께 스케줄링 동작 및 데이터 정합성 검증
 * TODO: 개발 환경용 테스트 API 추가 필요 - 스케줄러 수동 실행 엔드포인트
 * TODO: 개발 환경에서는 배치 실행 주기를 짧게 설정 (예: 2분) 하여 로컬 테스트 편의성 향상
 * TODO: 대용량 데이터 처리 시 배치 처리 성능 최적화 및 메모리 효율성 개선 필요
 */
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
