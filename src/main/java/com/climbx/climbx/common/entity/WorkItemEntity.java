package com.climbx.climbx.common.entity;

import com.climbx.climbx.common.enums.WorkItemStatus;
import com.climbx.climbx.common.enums.WorkItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "work_items",
    uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(
            name = "uk_work_items_type_key_hash",
            columnNames = {"type", "key_hash"}
        )
    },
    indexes = {
        @jakarta.persistence.Index(name = "idx_work_items_pickup", columnList = "status,next_attempt_at,created_at"),
        @jakarta.persistence.Index(name = "idx_work_items_claimed", columnList = "claimed_by,claimed_at")
    }
)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class WorkItemEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "varchar(64)", nullable = false)
    private WorkItemType type;

    @Column(name = "key_text", length = 256, nullable = false)
    private String keyText;

    @Column(name = "key_hash", columnDefinition = "BINARY(32)", nullable = false)
    private byte[] keyHash;

    @Column(name = "payload_json", columnDefinition = "JSON")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "varchar(32)", nullable = false)
    private WorkItemStatus status;

    @Builder.Default
    @Min(0)
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "claimed_by", length = 64)
    private String claimedBy;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "heartbeat_at")
    private LocalDateTime heartbeatAt;

    @Column(name = "last_error", length = 512)
    private String lastError;

    // fluent accessors for updates
    public WorkItemEntity status(WorkItemStatus status) {
        this.status = status;
        return this;
    }

    public WorkItemEntity lastError(String lastError) {
        this.lastError = lastError;
        return this;
    }

    public WorkItemEntity attempts(Integer attempts) {
        this.attempts = attempts;
        return this;
    }

    public WorkItemEntity nextAttemptAt(LocalDateTime t) {
        this.nextAttemptAt = t;
        return this;
    }
}


