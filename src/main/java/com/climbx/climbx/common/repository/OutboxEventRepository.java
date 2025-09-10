package com.climbx.climbx.common.repository;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query("SELECT o FROM OutboxEventEntity o WHERE o.processed = false ORDER BY o.occurredAt ASC")
    List<OutboxEventEntity> findAllUnprocessedOrderByOccurredAtAsc();

    @Query("SELECT o FROM OutboxEventEntity o WHERE o.processed = false ORDER BY o.occurredAt ASC")
    Page<OutboxEventEntity> findAllUnprocessedOrderByOccurredAtAsc(Pageable pageable);
}