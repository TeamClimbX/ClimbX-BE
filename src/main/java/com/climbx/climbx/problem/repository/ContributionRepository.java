package com.climbx.climbx.problem.repository;

import com.climbx.climbx.problem.entity.ContributionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionRepository extends JpaRepository<ContributionEntity, Long> {

    List<ContributionEntity> findAllByProblemEntity_ProblemId(UUID problemId);

    List<ContributionEntity> findAllByProblemEntity_ProblemIdOrderByCreatedAtDesc(
        UUID problemId,
        Pageable pageable
    );

    // 조회용: 기본 투표(anonymous) 제외
    @EntityGraph(attributePaths = {"contributionTags", "userAccountEntity", "userAccountEntity.userStatEntity"})
    List<ContributionEntity> findAllByProblemEntity_ProblemIdAndUserAccountEntityIsNotNullOrderByCreatedAtDesc(
        UUID problemId,
        Pageable pageable
    );

    // 읽기 좋은 래퍼 메서드
    default List<ContributionEntity> findRecentUserVotes(UUID problemId, Pageable pageable) {
        return findAllByProblemEntity_ProblemIdAndUserAccountEntityIsNotNullOrderByCreatedAtDesc(
            problemId, pageable
        );
    }

    @EntityGraph(attributePaths = {"contributionTags"})
    Optional<ContributionEntity> findByUserAccountEntity_UserIdAndProblemEntity_ProblemId(
        Long userId, UUID problemId);

    default Optional<ContributionEntity> findByUserIdAndProblemId(Long userId, UUID problemId) {
        return findByUserAccountEntity_UserIdAndProblemEntity_ProblemId(userId, problemId);
    }
}
