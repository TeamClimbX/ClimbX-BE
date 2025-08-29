package com.climbx.climbx.common.entity;

import com.climbx.climbx.common.enums.OutboxEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "outbox_events",
    indexes = {
        @Index(name = "idx_outbox_processed", columnList = "processed"),
        @Index(name = "idx_outbox_occurred_at", columnList = "occurred_at")
    }
)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class OutboxEventEntity extends BaseTimeEntity {

    @Id
    @Column(name = "event_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID eventId;

    @Column(name = "aggregate_type", length = 64, nullable = false)
    @NotNull
    private String aggregateType;

    @Column(name = "aggregate_id", length = 64, nullable = false)
    @NotNull
    private String aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", columnDefinition = "varchar(64)", nullable = false)
    @NotNull
    private OutboxEventType eventType;

    // payload는 더 이상 사용하지 않습니다.

    @Column(name = "occurred_at", nullable = false)
    @NotNull
    private LocalDateTime occurredAt;

    @Builder.Default
    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public void markProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }
}


