package com.climbx.climbx.fixture;

import static org.assertj.core.api.Assertions.assertThat;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SchedulerTestFixture {

    public static UserStatEntity createUserStatWithRating(Long userId, int rating) {
        UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId);
        return UserStatEntity.builder()
            .userId(userId)
            .userAccountEntity(userAccount)
            .rating(rating)
            .currentStreak(5)
            .longestStreak(15)
            .solvedCount(25)
            .submissionCount(30)
            .contributionCount(5)
            .rivalCount(3)
            .build();
    }

    public static UserStatEntity createUserStatWithValues(Long userId, int rating,
        int longestStreak, int solvedCount) {
        UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId);
        return UserStatEntity.builder()
            .userId(userId)
            .userAccountEntity(userAccount)
            .rating(rating)
            .currentStreak(5)
            .longestStreak(longestStreak)
            .solvedCount(solvedCount)
            .submissionCount(30)
            .contributionCount(5)
            .rivalCount(3)
            .build();
    }

    public static OutboxEventEntity createUnprocessedOutboxEvent(OutboxEventType eventType) {
        return OutboxEventEntity.builder()
            .eventId(UUID.randomUUID())
            .aggregateType("Problem")
            .aggregateId(UUID.randomUUID().toString())
            .eventType(eventType)
            .occurredAt(LocalDateTime.now().minusMinutes(10))
            .processed(false)
            .build();
    }

    public static OutboxEventEntity createProcessedOutboxEvent(OutboxEventType eventType) {
        return OutboxEventEntity.builder()
            .eventId(UUID.randomUUID())
            .aggregateType("Problem")
            .aggregateId(UUID.randomUUID().toString())
            .eventType(eventType)
            .occurredAt(LocalDateTime.now().minusMinutes(15))
            .processed(true)
            .processedAt(LocalDateTime.now().minusMinutes(5))
            .build();
    }

    public static void assertRankingHistory(UserRankingHistoryEntity history,
        CriteriaType expectedCriteria,
        int expectedValue,
        Long expectedUserId) {
        assertThat(history.criteria()).isEqualTo(expectedCriteria);
        assertThat(history.value()).isEqualTo(expectedValue);
        assertThat(history.userAccountEntity()).isNotNull();
        assertThat(history.userAccountEntity().userId()).isEqualTo(expectedUserId);
    }

    public static List<UserStatEntity> createMultipleUserStats() {
        return List.of(
            createUserStatWithValues(1L, 1500, 10, 20),
            createUserStatWithValues(2L, 1600, 15, 30),
            createUserStatWithValues(3L, 1400, 8, 18)
        );
    }

    public static List<OutboxEventEntity> createMultipleUnprocessedEvents() {
        return List.of(
            createUnprocessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED),
            createUnprocessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED),
            createUnprocessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED)
        );
    }
}