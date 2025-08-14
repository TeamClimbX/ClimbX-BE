package com.climbx.climbx.problem.repository;

import com.climbx.climbx.problem.entity.ContributionTagEntity;
import com.climbx.climbx.problem.entity.ProblemTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionTagRepository extends
    JpaRepository<ContributionTagEntity, ProblemTagId> {

}
