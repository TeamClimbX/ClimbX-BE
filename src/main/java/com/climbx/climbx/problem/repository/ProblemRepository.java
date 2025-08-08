package com.climbx.climbx.problem.repository;

import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.enums.ProblemTierType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<ProblemEntity, UUID> {

    @Query("""
                SELECT new com.climbx.climbx.problem.dto.ProblemInfoResponseDto(
                    p.problemId,
                    g.gymId,
                    g.name,
                    ga.gymAreaId,
                    ga.areaName,
                    p.localLevel,
                    p.holdColor,
                    p.tier,
                    p.rating,
                    p.problemImageCdnUrl,
                    p.activeStatus,
                    p.createdAt
                )
                FROM ProblemEntity p
                JOIN  p.gymArea ga
                JOIN  p.gymEntity g
                WHERE (:gymId IS NULL OR p.gymEntity.gymId = :gymId) AND
                (:gymAreaId IS NULL OR p.gymArea.gymAreaId = :gymAreaId) AND
                (:localLevel IS NULL OR p.localLevel = :localLevel) AND
                (:holdColor IS NULL OR p.holdColor = :holdColor) AND
                (:problemTier IS NULL OR p.tier = :problemTier) AND
                (:activeStatus IS NULL OR p.activeStatus = :activeStatus)
        """)
    List<ProblemInfoResponseDto> findByGymAndAreaAndLevelAndColorAndProblemTierAndActiveStatus(
        @Param("gymId") Long gymId,
        @Param("gymAreaId") Long gymAreaId,
        @Param("localLevel") String localLevel,
        @Param("holdColor") String holdColor,
        @Param("problemTier") ProblemTierType problemTier,
        @Param("activeStatus") ActiveStatusType activeStatus
    );
}
