package com.climbx.climbx.common.service;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.common.exception.OutboxEventRecordException;
import com.climbx.climbx.common.repository.OutboxEventRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void recordEvent(
        String aggregateType,
        String aggregateId,
        OutboxEventType eventType
    ) {
        try {
            OutboxEventEntity event = OutboxEventEntity.builder()
                .eventId(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .occurredAt(LocalDateTime.now())
                .build();

            outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error(
                "Failed to record outbox event for aggregateType: {}, aggregateId: {}, eventType: {}, error: {}",
                aggregateType, aggregateId, eventType, e.getMessage(), e);
            throw new OutboxEventRecordException(aggregateType, aggregateId, eventType, e);
        }
    }
}
