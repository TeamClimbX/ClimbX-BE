package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.repository.OutboxEventRepository;
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
 * OutboxEventScheduler는 Outbox 이벤트를 주기적으로 스캔하여 OutboxEventProcessor에 위임하는 스케줄러입니다.
 * <p>
 * - processAllOutboxEvents: 매 정각 Outbox에서 미처리 이벤트를 조회하고 각각을 독립적인 트랜잭션으로 처리 - 각 이벤트는
 * OutboxEventProcessor.processEventInNewTransaction()을 통해 개별 트랜잭션으로 처리됨 - 한 이벤트 처리 실패가 다른 이벤트 처리에
 * 영향을 주지 않음 * <p>
 * TODO: 배치 처리 재시도 메커니즘 개선 필요
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
    private final OutboxEventProcessor outboxEventProcessor;

    @Value("${scheduler.outbox.batch-size:500}")
    private int batchSize;

    /**
     * Outbox에서 미처리 이벤트들을 모두 처리합니다. 이벤트 타입에 따라 적절한 처리 로직을 호출합니다. dev 환경: 매 1분, prod 환경: 매 정각
     */
    @Scheduled(cron = "${scheduler.outbox.cron:0 0 * * * *}", zone = "Asia/Seoul")
    public void processAllOutboxEvents() {
        int successCount = 0;
        int failureCount = 0;
        long totalEvents = 0;
        Page<OutboxEventEntity> eventsPage;
        int pageNumber = 0;

        do {
            Pageable pageable = PageRequest.of(pageNumber++, batchSize);
            eventsPage = outboxEventRepository.findAllUnprocessedOrderByOccurredAtAsc(pageable);
            totalEvents += eventsPage.getNumberOfElements();

            if (eventsPage.hasContent()) {
                log.debug("Processing outbox event batch {} with {} events (total processed: {})",
                    pageNumber, eventsPage.getNumberOfElements(), totalEvents);
            }

            for (OutboxEventEntity event : eventsPage.getContent()) {
                try {
                    outboxEventProcessor.processEventInNewTransaction(event);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to process {} event for aggregateId: {}, error: {}",
                        event.eventType(), event.aggregateId(), e.getMessage(), e);
                }
            }
        } while (eventsPage.hasNext());

        if (totalEvents > 0) {
            log.info("Processed {} outbox events - Success: {}, Failure: {}",
                totalEvents, successCount, failureCount);
        }
    }

}
