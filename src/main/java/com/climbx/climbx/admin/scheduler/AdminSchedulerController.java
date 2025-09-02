package com.climbx.climbx.admin.scheduler;

import com.climbx.climbx.common.scheduler.DailyRankingSnapshotScheduler;
import com.climbx.climbx.common.scheduler.OutboxEventScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/scheduler")
@RequiredArgsConstructor
@Profile("dev")
public class AdminSchedulerController implements AdminSchedulerApiDocumentation {

    private final OutboxEventScheduler outboxEventScheduler;
    private final DailyRankingSnapshotScheduler dailyRankingSnapshotScheduler;

    @Override
    @PostMapping("/outbox/trigger")
    public void triggerOutboxEventScheduler() {
        outboxEventScheduler.processAllOutboxEvents();
    }

    @Override
    @PostMapping("/ranking/trigger")
    public void triggerDailyRankingSnapshotScheduler() {
        dailyRankingSnapshotScheduler.snapshotDailyRanking();
    }
}