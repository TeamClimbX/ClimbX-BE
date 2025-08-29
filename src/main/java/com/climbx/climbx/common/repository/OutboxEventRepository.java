package com.climbx.climbx.common.repository;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.enums.OutboxEventType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query("SELECT o FROM OutboxEventEntity o WHERE o.eventType = :eventType AND o.dedupHash = :dedupHash")
    Optional<OutboxEventEntity> findByEventTypeAndDedupHash(OutboxEventType eventType, byte[] dedupHash);
}


