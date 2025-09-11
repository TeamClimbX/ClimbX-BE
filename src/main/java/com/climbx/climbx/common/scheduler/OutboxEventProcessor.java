package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.repository.OutboxEventRepository;
import com.climbx.climbx.common.scheduler.exception.OutboxEventProcessingException;
import com.climbx.climbx.common.scheduler.exception.UnknownOutboxEventTypeException;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final SubmissionRepository submissionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserRatingProcessor userRatingProcessor;
    
    @Value("${scheduler.outbox.user-rating-batch-size:100}")
    private int userRatingBatchSize;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEventInNewTransaction(OutboxEventEntity event) {
        try {
            switch (event.eventType()) {
                case PROBLEM_TIER_CHANGED -> processProblemTierEvent(event);
                default -> {
                    String eventType = event.eventType().toString();
                    String aggregateId = event.aggregateId();
                    log.warn("Unknown event type: {} for aggregateId: {}", eventType, aggregateId);
                    throw new UnknownOutboxEventTypeException(eventType, aggregateId);
                }
            }
            event.markProcessed();
            // Detached 엔티티 변경 사항을 새 트랜잭션에 반영
            outboxEventRepository.save(event);
            log.debug("Successfully processed {} event for aggregateId: {}",
                event.eventType(), event.aggregateId());
        } catch (Exception e) {
            String eventType = event.eventType().toString();
            String aggregateId = event.aggregateId();
            log.error("Failed to process {} event for aggregateId: {}, error: {}",
                eventType, aggregateId, e.getMessage(), e);
            throw new OutboxEventProcessingException(eventType, aggregateId, e);
        }
    }

    private void processProblemTierEvent(OutboxEventEntity event) {
        UUID problemId = UUID.fromString(event.aggregateId());
        updateUserRatingsForProblem(problemId);
        log.info("Successfully updated user ratings for PROBLEM_TIER_CHANGED event, problemId: {}",
            problemId);
    }

    private void updateUserRatingsForProblem(UUID problemId) {
        int successCount = 0;
        int failureCount = 0;
        long totalUsers = 0;
        Page<Long> userIdsPage;
        int pageNumber = 0;

        do {
            Pageable pageable = PageRequest.of(pageNumber++, userRatingBatchSize);
            userIdsPage = submissionRepository.findDistinctUserIdsByProblemId(problemId, pageable);
            totalUsers += userIdsPage.getNumberOfElements();
            
            if (userIdsPage.hasContent()) {
                log.debug("Processing user rating batch {} with {} users for problem: {} (total processed: {})", 
                    pageNumber, userIdsPage.getNumberOfElements(), problemId, totalUsers);
            }

            for (Long userId : userIdsPage.getContent()) {
                try {
                    userRatingProcessor.updateUserRatingInNewTransaction(userId);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to update rating for userId: {}, error: {}", userId,
                        e.getMessage(), e);
                }
            }
        } while (userIdsPage.hasNext());

        log.info("Updated ratings for problem: {} - Total: {}, Success: {}, Failure: {}",
            problemId, totalUsers, successCount, failureCount);
    }
}