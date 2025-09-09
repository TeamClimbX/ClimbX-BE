package com.climbx.climbx.common.scheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.common.repository.OutboxEventRepository;
import com.climbx.climbx.fixture.UserFixture;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboxEventSchedulerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OutboxEventProcessor outboxEventProcessor;

    @InjectMocks
    private OutboxEventScheduler outboxEventScheduler;

    @Nested
    @DisplayName("processAllOutboxEvents 메서드는")
    class ProcessAllOutboxEventsTest {

        @Test
        @DisplayName("PROBLEM_TIER_CHANGED 이벤트를 성공적으로 처리한다")
        void shouldProcessProblemTierChangedEventSuccessfully() {
            // given
            String problemId = UUID.randomUUID().toString();
            OutboxEventEntity event = UserFixture.createOutboxEventEntity(
                problemId, OutboxEventType.PROBLEM_TIER_CHANGED);

            given(outboxEventRepository.findAllUnprocessedOrderByOccurredAtAsc())
                .willReturn(List.of(event));

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then
            then(outboxEventProcessor).should()
                .processEventInNewTransaction(event);
        }

        @Test
        @DisplayName("처리할 이벤트가 없을 때 정상적으로 완료한다")
        void shouldCompleteNormallyWhenNoEventsToProcess() {
            // given
            given(outboxEventRepository.findAllUnprocessedOrderByOccurredAtAsc())
                .willReturn(Collections.emptyList());

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then
            then(outboxEventProcessor).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("개별 이벤트 처리 실패 시에도 다른 이벤트는 계속 처리한다")
        void shouldContinueProcessingOtherEventsWhenIndividualEventFails() {
            // given
            String problemId1 = UUID.randomUUID().toString();
            String problemId2 = UUID.randomUUID().toString();
            OutboxEventEntity event1 = UserFixture.createOutboxEventEntity(
                problemId1, OutboxEventType.PROBLEM_TIER_CHANGED);
            OutboxEventEntity event2 = UserFixture.createOutboxEventEntity(
                problemId2, OutboxEventType.PROBLEM_TIER_CHANGED);

            given(outboxEventRepository.findAllUnprocessedOrderByOccurredAtAsc())
                .willReturn(List.of(event1, event2));
            willThrow(new RuntimeException("Processing error"))
                .given(outboxEventProcessor).processEventInNewTransaction(event1);

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then
            then(outboxEventProcessor).should()
                .processEventInNewTransaction(event1);
            then(outboxEventProcessor).should()
                .processEventInNewTransaction(event2);
        }
    }
}