package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.service.UserDataAggregationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final SubmissionRepository submissionRepository;
    private final UserRatingProcessor userRatingProcessor;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEventInNewTransaction(OutboxEventEntity event) {
        try {
            switch (event.eventType()) {
                case PROBLEM_TIER_CHANGED -> processProblemTierEvent(event);
                default -> log.warn("Unknown event type: {} for aggregateId: {}", 
                    event.eventType(), event.aggregateId());
            }
            event.markProcessed();
            log.debug("Successfully processed {} event for aggregateId: {}",
                event.eventType(), event.aggregateId());
        } catch (Exception e) {
            log.error("Failed to process {} event for aggregateId: {}, error: {}",
                event.eventType(), event.aggregateId(), e.getMessage(), e);
            throw e;
        }
    }

    private void processProblemTierEvent(OutboxEventEntity event) {
        UUID problemId = UUID.fromString(event.aggregateId());
        updateUserRatingsForProblem(problemId);
        log.info("Successfully updated user ratings for PROBLEM_TIER_CHANGED event, problemId: {}",
            problemId);
    }

    private void updateUserRatingsForProblem(UUID problemId) {
        List<Long> userIds = submissionRepository.findDistinctUserIdsByProblemId(problemId);
        int successCount = 0;
        int failureCount = 0;

        for (Long userId : userIds) {
            try {
                userRatingProcessor.updateUserRatingInNewTransaction(userId);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to update rating for userId: {}, error: {}", userId, e.getMessage(), e);
            }
        }

        log.info("Updated ratings for problem: {} - Total: {}, Success: {}, Failure: {}", 
            problemId, userIds.size(), successCount, failureCount);
    }
}