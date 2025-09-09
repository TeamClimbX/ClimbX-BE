package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSnapshotProcessor {

    private final UserRankingHistoryRepository userRankingHistoryRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createUserRankingSnapshotInNewTransaction(UserStatEntity userStat) {
        try {
            Long userId = userStat.userId();
            log.debug("Start creating ranking snapshot for userId: {}", userId);
            UserAccountEntity userAccountRef = userAccountRepository.getReferenceById(userId);
            // FK 세팅용 proxy 객체 

            List<UserRankingHistoryEntity> rankingHistories = List.of(
                UserRankingHistoryEntity.builder()
                    .criteria(CriteriaType.RATING)
                    .userAccountEntity(userAccountRef)
                    .value(userStat.rating())
                    .build(),
                UserRankingHistoryEntity.builder()
                    .criteria(CriteriaType.LONGEST_STREAK)
                    .userAccountEntity(userAccountRef)
                    .value(userStat.longestStreak())
                    .build(),
                UserRankingHistoryEntity.builder()
                    .criteria(CriteriaType.SOLVED_COUNT)
                    .userAccountEntity(userAccountRef)
                    .value(userStat.solvedCount())
                    .build()
            );

            userRankingHistoryRepository.saveAll(rankingHistories);

            log.debug("Successfully created ranking snapshot for userId: {}", userId);
        } catch (Exception e) {
            Long safeUserId = userStat.userId();
            log.error("Failed to create ranking snapshot for userId: {}, error: {}",
                safeUserId, e.getMessage(), e);
            throw e;
        }
    }
}