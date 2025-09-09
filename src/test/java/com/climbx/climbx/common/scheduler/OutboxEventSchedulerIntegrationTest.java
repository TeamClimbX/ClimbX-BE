package com.climbx.climbx.common.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.climbx.climbx.common.entity.OutboxEventEntity;
import com.climbx.climbx.common.enums.OutboxEventType;
import com.climbx.climbx.common.repository.OutboxEventRepository;
import com.climbx.climbx.fixture.SchedulerTestFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class OutboxEventSchedulerIntegrationTest extends AbstractSchedulerIntegrationTest {

    @Autowired
    private OutboxEventScheduler outboxEventScheduler;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoSpyBean
    private OutboxEventProcessor outboxEventProcessor;

    @Nested
    @DisplayName("processOutboxEvents 메서드는")
    class ProcessOutboxEventsTest {

        @Test
        @DisplayName("미처리된 아웃박스 이벤트들을 처리하고 상태를 업데이트한다")
        void shouldProcessUnprocessedOutboxEventsAndUpdateStatus() {
            // given
            List<OutboxEventEntity> unprocessedEvents = SchedulerTestFixture.createMultipleUnprocessedEvents();
            outboxEventRepository.saveAll(unprocessedEvents);

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then - 실제 값 검증
            List<OutboxEventEntity> allEvents = outboxEventRepository.findAll();
            assertThat(allEvents).hasSize(3);

            for (OutboxEventEntity event : allEvents) {
                OutboxEventEntity updatedEvent = outboxEventRepository.findById(event.eventId()).orElseThrow();
                assertThat(updatedEvent.processed()).isTrue();
                assertThat(updatedEvent.processedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("처리할 이벤트가 없을 때 정상적으로 완료한다")
        void shouldCompleteNormallyWhenNoUnprocessedEvents() {
            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then - 실제 값 검증
            assertThat(countUnprocessedOutboxEvents()).isEqualTo(0);
            assertThat(countProcessedOutboxEvents()).isEqualTo(0);
        }

        @Test
        @DisplayName("PROBLEM_TIER_CHANGED 이벤트를 올바르게 처리한다")
        void shouldProcessProblemTierChangedEvents() {
            // given
            OutboxEventEntity tierChangedEvent =
                SchedulerTestFixture.createUnprocessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED);
            outboxEventRepository.save(tierChangedEvent);

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then - 실제 값 검증
            OutboxEventEntity processedEvent =
                outboxEventRepository.findById(tierChangedEvent.eventId()).orElseThrow();

            assertThat(processedEvent.processed()).isTrue();
            assertThat(processedEvent.processedAt()).isNotNull();
            assertThat(processedEvent.eventType()).isEqualTo(OutboxEventType.PROBLEM_TIER_CHANGED);
        }

        @Test
        @DisplayName("일부 이벤트 처리 실패 시 다른 이벤트는 정상 처리된다")
        void shouldContinueProcessingOtherEventsWhenOneEventFails() {
            // given
            List<OutboxEventEntity> events = SchedulerTestFixture.createMultipleUnprocessedEvents();
            List<OutboxEventEntity> savedEvents = outboxEventRepository.saveAll(events);

            // 실패를 유도하기 위해 두 번째 이벤트의 aggregateId를 잘못된 UUID로 업데이트
            OutboxEventEntity failingEvent = savedEvents.get(1);
            OutboxEventEntity invalidEvent = OutboxEventEntity.builder()
                .eventId(failingEvent.eventId())
                .aggregateType(failingEvent.aggregateType())
                .aggregateId("invalid-uuid")
                .eventType(failingEvent.eventType())
                .occurredAt(failingEvent.occurredAt())
                .processed(false)
                .build();
            outboxEventRepository.save(invalidEvent);

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then - 실제 값 검증
            List<OutboxEventEntity> allEvents = outboxEventRepository.findAll();
            assertThat(allEvents).hasSize(3);

            OutboxEventEntity firstEvent = outboxEventRepository.findById(savedEvents.get(0).eventId()).orElseThrow();
            OutboxEventEntity thirdEvent = outboxEventRepository.findById(savedEvents.get(2).eventId()).orElseThrow();
            OutboxEventEntity failedEvent = outboxEventRepository.findById(failingEvent.eventId()).orElseThrow();

            assertThat(firstEvent.processed()).isTrue();
            assertThat(thirdEvent.processed()).isTrue();
            assertThat(failedEvent.processed()).isFalse();
            assertThat(failedEvent.processedAt()).isNull();
        }

        @Test
        @DisplayName("이미 처리된 이벤트는 건너뛴다")
        void shouldSkipAlreadyProcessedEvents() {
            // given
            OutboxEventEntity processedEvent =
                SchedulerTestFixture.createProcessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED);
            OutboxEventEntity unprocessedEvent =
                SchedulerTestFixture.createUnprocessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED);

            outboxEventRepository.saveAll(List.of(processedEvent, unprocessedEvent));

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then - 실제 값 검증
            assertThat(countProcessedOutboxEvents()).isEqualTo(2);
            assertThat(countUnprocessedOutboxEvents()).isEqualTo(0);

            OutboxEventEntity originalProcessedEvent =
                outboxEventRepository.findById(processedEvent.eventId()).orElseThrow();
            assertThat(originalProcessedEvent.processed()).isTrue();
            assertThat(originalProcessedEvent.processedAt()).isEqualTo(processedEvent.processedAt());
        }

        @Test
        @DisplayName("이벤트 타입별로 올바르게 처리된다")
        void shouldProcessEventsByTypeCorrectly() {
            // given
            OutboxEventEntity event1 =
                SchedulerTestFixture.createUnprocessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED);
            OutboxEventEntity event2 =
                SchedulerTestFixture.createUnprocessedOutboxEvent(OutboxEventType.PROBLEM_TIER_CHANGED);

            outboxEventRepository.saveAll(List.of(event1, event2));

            // when
            outboxEventScheduler.processAllOutboxEvents();

            // then - 실제 값 검증
            List<OutboxEventEntity> allEvents = outboxEventRepository.findAll();
            assertThat(allEvents).hasSize(2);

            for (OutboxEventEntity event : allEvents) {
                OutboxEventEntity updatedEvent = outboxEventRepository.findById(event.eventId()).orElseThrow();
                assertThat(updatedEvent.processed()).isTrue();
                assertThat(updatedEvent.eventType()).isEqualTo(OutboxEventType.PROBLEM_TIER_CHANGED);
            }
        }
    }
}