package com.climbx.climbx.common.worker;

import com.climbx.climbx.common.entity.WorkItemEntity;
import com.climbx.climbx.common.enums.WorkItemStatus;
import com.climbx.climbx.common.enums.WorkItemType;
import com.climbx.climbx.common.service.WorkItemService;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.problem.repository.ProblemRepository;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.util.UserRatingUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class WorkItemWorker {

    private final WorkItemService workItemService;
    private final ProblemRepository problemRepository;
    private final UserStatRepository userStatRepository;
    private final UserRatingUtil userRatingUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollAndWork() {
        List<WorkItemEntity> candidates = workItemService.findPickupCandidates();
        for (WorkItemEntity item : candidates) {
            boolean claimed = workItemService.claim(item.id(), "worker-1");
            if (!claimed) {
                continue;
            }
            try {
                handle(item);
                item.status(WorkItemStatus.DONE);
            } catch (Exception e) {
                log.error("Work item failed id={} type={} err={}", item.id(), item.type(), e.getMessage(), e);
                item.status(WorkItemStatus.FAILED);
                item.lastError(e.getMessage());
                item.attempts(item.attempts() + 1);
                item.nextAttemptAt(LocalDateTime.now().plusMinutes(10));
            }
        }
    }

    private void handle(WorkItemEntity item) throws Exception {
        if (item.type() == WorkItemType.UPDATE_PROBLEM_TIER) {
            JsonNode node = objectMapper.readTree(item.payloadJson());
            UUID problemId = UUID.fromString(node.get("problemId").asText());
            ProblemTierType newTier = ProblemTierType.valueOf(node.get("newTier").asText());
            problemRepository.findById(problemId).ifPresent(p -> p.updateRatingAndTierAndTags(
                newTier.value(), newTier, List.of()
            ));
            return;
        }

        if (item.type() == WorkItemType.REFRESH_USER_RATING) {
            Long userId = Long.valueOf(item.keyText());
            userStatRepository.findById(userId).ifPresent(us -> {
                var rating = userRatingUtil.calculateUserRating(
                    List.of(), us.submissionCount(), us.solvedCount(), us.contributionCount()
                );
                us.setRating(rating.totalRating());
                us.setTopProblemRating(rating.topProblemRating());
            });
            return;
        }

        if (item.type() == WorkItemType.RANKING_HISTORY_SNAPSHOT) {
            // 일일 스냅샷 처리는 별도 스케줄러에서 생성
            return;
        }
    }
}


