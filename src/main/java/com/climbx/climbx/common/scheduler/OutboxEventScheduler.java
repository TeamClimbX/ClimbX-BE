package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.common.repository.OutboxEventRepository;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.service.UserDataAggregationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * OutboxEventScheduler는 Outbox 이벤트를 주기적으로 스캔하여 이벤트 타입별로 적절한 처리를 수행하는 스케줄러입니다.
 *
 * - processAllOutboxEvents: 매 정각 Outbox에서 미처리 이벤트를 모두 처리
 *
 * TODO: Work Queue 패턴을 통한 트랜잭션 분리 및 동시성 제어 개선 필요
 * TODO: 배치 처리 성능 최적화 및 재시도 메커니즘 개선 필요
 * TODO: 인테그레이션 테스트 추가 필요 - 실제 DB와 함께 스케줄링 동작 검증
 * TODO: 개발 환경용 테스트 API 추가 필요 - 스케줄러 수동 실행 엔드포인트
 * TODO: 개발 환경에서는 배치 실행 주기를 짧게 설정 (예: 1분) 하여 로컬 테스트 편의성 향상
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final SubmissionRepository submissionRepository;
    private final UserDataAggregationService userDataAggregationService;

    /**
     * 1시간 단위(정각)에 Outbox에서 미처리 이벤트들을 모두 처리합니다. 이벤트 타입에 따라 적절한 처리 로직을 호출합니다.
     */
    @Transactional
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void processAllOutboxEvents() {
        List<OutboxEventEntity> events = outboxEventRepository.findAllUnprocessedOrderByOccurredAtAsc();

        for (OutboxEventEntity event : events) {
            try {
                switch (event.eventType()) {
                    case PROBLEM_TIER_CHANGED -> processProblemTierEvent(event);
                    // TODO: 향후 다른 이벤트 타입들 추가 시 여기서 처리
                    default -> log.warn("Unknown event type: {} for aggregateId: {}", 
                        event.eventType(), event.aggregateId());
                }
                event.markProcessed();
                log.debug("Successfully processed {} event for aggregateId: {}",
                    event.eventType(), event.aggregateId());
            } catch (Exception e) {
                log.error("Failed to process {} event for aggregateId: {}, error: {}",
                    event.eventType(), event.aggregateId(), e.getMessage(), e);
            }
        }

        if (!events.isEmpty()) {
            log.info("Processed {} outbox events", events.size());
        }
    }

    private void processProblemTierEvent(OutboxEventEntity event) {
        UUID problemId = UUID.fromString(event.aggregateId());
        updateUserRatingsForProblem(problemId);
        log.info("Successfully updated user ratings for PROBLEM_TIER_CHANGED event, problemId: {}",
            problemId);
    }

    private void updateUserRatingsForProblem(UUID problemId) {
        // 이 문제를 푼 유저들을 찾아서 레이팅 업데이트
        List<Long> userIds = submissionRepository.findDistinctUserIdsByProblemId(problemId);

        for (Long userId : userIds) {
            try {
                // 통합된 레이팅 재계산 메서드 사용
                userDataAggregationService.recalculateAndUpdateUserRating(userId);
            } catch (Exception e) {
                log.error("Failed to update rating for userId: {}, error: {}", userId,
                    e.getMessage(), e);
            }
        }

        log.info("Updated ratings for {} users who solved problem: {}", userIds.size(), problemId);
    }
}
