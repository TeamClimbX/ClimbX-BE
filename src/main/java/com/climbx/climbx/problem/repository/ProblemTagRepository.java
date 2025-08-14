package com.climbx.climbx.problem.repository;

import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.entity.ProblemTagEntity;
import com.climbx.climbx.problem.entity.ProblemTagId;
import com.climbx.climbx.problem.enums.ProblemTagType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemTagRepository extends JpaRepository<ProblemTagEntity, ProblemTagId> {

    Optional<ProblemTagEntity> findByProblemEntityAndTag(ProblemEntity problemEntity,
        ProblemTagType tag);

    List<ProblemTagEntity> findTop2ByProblemEntityOrderByPriorityDesc(ProblemEntity problemEntity);
}
