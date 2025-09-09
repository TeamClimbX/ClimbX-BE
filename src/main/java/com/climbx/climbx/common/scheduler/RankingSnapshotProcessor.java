package com.climbx.climbx.common.scheduler;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createUserRankingSnapshotInNewTransaction(UserStatEntity userStat) {
        try {
            List<UserRankingHistoryEntity> rankingHistories = List.of(
                UserRankingHistoryEntity.builder()
                    .criteria(CriteriaType.RATING)
                    .userAccountEntity(userStat.userAccountEntity())
                    .value(userStat.rating())
                    .build(),
                UserRankingHistoryEntity.builder()
                    .criteria(CriteriaType.LONGEST_STREAK)
                    .userAccountEntity(userStat.userAccountEntity())
                    .value(userStat.longestStreak())
                    .build(),
                UserRankingHistoryEntity.builder()
                    .criteria(CriteriaType.SOLVED_COUNT)
                    .userAccountEntity(userStat.userAccountEntity())
                    .value(userStat.solvedCount())
                    .build()
            );

            userRankingHistoryRepository.saveAll(rankingHistories);

            log.debug("Successfully created ranking snapshot for userId: {}",
                userStat.userAccountEntity().userId());
        } catch (Exception e) {
            log.error("Failed to create ranking snapshot for userId: {}, error: {}",
                userStat.userAccountEntity().userId(), e.getMessage(), e);
            throw e;
        }
    }
}