package com.climbx.climbx.common.service;

import com.climbx.climbx.common.entity.WorkItemEntity;
import com.climbx.climbx.common.enums.WorkItemStatus;
import com.climbx.climbx.common.enums.WorkItemType;
import com.climbx.climbx.common.repository.WorkItemRepository;
import com.climbx.climbx.common.util.HashUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkItemService {

    private final WorkItemRepository workItemRepository;

    @Transactional
    public WorkItemEntity enqueueUnique(WorkItemType type, String keyText, String payloadJson) {
        byte[] keyHash = HashUtil.sha256(type.name() + "|" + keyText);

        Optional<WorkItemEntity> existing = workItemRepository.findByTypeAndKeyHash(type, keyHash);
        if (existing.isPresent()) {
            return existing.get();
        }

        WorkItemEntity item = WorkItemEntity.builder()
            .type(type)
            .keyText(keyText)
            .keyHash(keyHash)
            .payloadJson(payloadJson)
            .status(WorkItemStatus.PENDING)
            .attempts(0)
            .nextAttemptAt(LocalDateTime.now())
            .build();

        return workItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<WorkItemEntity> findPickupCandidates() {
        return workItemRepository.findPickupCandidates(LocalDateTime.now());
    }

    @Transactional
    public boolean claim(Long id, String workerId) {
        int updated = workItemRepository.claim(
            id,
            workerId,
            WorkItemStatus.PENDING,
            WorkItemStatus.IN_PROGRESS,
            LocalDateTime.now()
        );
        return updated > 0;
    }
}


