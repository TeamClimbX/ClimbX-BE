package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.user.service.UserDataAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRatingProcessor {

    private final UserDataAggregationService userDataAggregationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserRatingInNewTransaction(Long userId) {
        try {
            userDataAggregationService.recalculateAndUpdateUserRating(userId);
            log.debug("Successfully updated rating for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to update rating for userId: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}