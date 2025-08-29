package com.climbx.climbx.common.repository;

import com.climbx.climbx.common.entity.WorkItemEntity;
import com.climbx.climbx.common.enums.WorkItemStatus;
import com.climbx.climbx.common.enums.WorkItemType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkItemRepository extends JpaRepository<WorkItemEntity, Long> {

    @Query("SELECT w FROM WorkItemEntity w WHERE w.type = :type AND w.keyHash = :keyHash")
    Optional<WorkItemEntity> findByTypeAndKeyHash(@Param("type") WorkItemType type,
        @Param("keyHash") byte[] keyHash);

    @Query("""
        SELECT w FROM WorkItemEntity w
        WHERE w.status = 'PENDING'
          AND (w.nextAttemptAt IS NULL OR w.nextAttemptAt <= :now)
        ORDER BY w.createdAt ASC
        """)
    List<WorkItemEntity> findPickupCandidates(@Param("now") LocalDateTime now);

    @Modifying
    @Query("""
        UPDATE WorkItemEntity w
           SET w.status = :inProgress,
               w.claimedBy = :claimedBy,
               w.claimedAt = :now,
               w.heartbeatAt = :now
         WHERE w.id = :id
           AND w.status = :pending
        """)
    int claim(@Param("id") Long id,
        @Param("claimedBy") String claimedBy,
        @Param("pending") WorkItemStatus pending,
        @Param("inProgress") WorkItemStatus inProgress,
        @Param("now") LocalDateTime now);
}


