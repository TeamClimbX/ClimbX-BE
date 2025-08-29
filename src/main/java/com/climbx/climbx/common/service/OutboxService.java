package com.climbx.climbx.common.service;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.common.repository.OutboxEventRepository;
import com.climbx.climbx.common.util.HashUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void recordEvent(
        String aggregateType,
        String aggregateId,
        OutboxEventType eventType,
        String dedupKey,
        String payloadJson
    ) {
        byte[] dedupHash = HashUtil.sha256(eventType.name() + "|" + dedupKey);

        Optional<OutboxEventEntity> existing =
            outboxEventRepository.findByEventTypeAndDedupHash(eventType, dedupHash);
        if (existing.isPresent()) {
            return;
        }

        OutboxEventEntity event = OutboxEventEntity.builder()
            .id(UUID.randomUUID())
            .aggregateType(aggregateType)
            .aggregateId(aggregateId)
            .eventType(eventType)
            .dedupKey(dedupKey)
            .dedupHash(dedupHash)
            .payloadJson(payloadJson)
            .occurredAt(LocalDateTime.now())
            .build();

        outboxEventRepository.save(event);
    }
}


