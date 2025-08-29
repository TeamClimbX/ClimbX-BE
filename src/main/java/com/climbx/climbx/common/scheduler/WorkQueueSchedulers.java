package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.entity.WorkItemEntity;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.common.enums.WorkItemType;
import com.climbx.climbx.common.repository.OutboxEventRepository;
import com.climbx.climbx.common.service.WorkItemService;
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
public class WorkQueueSchedulers {

    private final OutboxEventRepository outboxEventRepository;
    private final WorkItemService workItemService;

    // 1시간 단위: 정각마다 실행
    @Transactional
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void publishHourlyWorkItems() {
        List<OutboxEventEntity> events = outboxEventRepository.findAll();
        events.forEach(event -> {
            if (event.eventType() == OutboxEventType.PROBLEM_TIER_CHANGED) {
                workItemService.enqueueUnique(
                    WorkItemType.UPDATE_PROBLEM_TIER,
                    event.aggregateId(),
                    event.payloadJson()
                );
            } else if (event.eventType() == OutboxEventType.USER_DIFFICULTY_CONTRIBUTED
                || event.eventType() == OutboxEventType.USER_SOLVED_PROBLEM) {
                workItemService.enqueueUnique(
                    WorkItemType.REFRESH_USER_RATING,
                    event.aggregateId(),
                    event.payloadJson()
                );
            }
        });
    }

    // 1일 단위: 매일 00:05 실행 (UTC 기준)
    @Transactional
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void enqueueDailyRankingSnapshot() {
        String key = LocalDate.now().toString();
        workItemService.enqueueUnique(
            WorkItemType.RANKING_HISTORY_SNAPSHOT,
            key,
            "{}"
        );
    }
}


