package com.climbx.climbx.problem.dto;

import com.climbx.climbx.problem.entity.ContributionEntity;
import com.climbx.climbx.problem.entity.ContributionTagEntity;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import java.util.List;
import lombok.Builder;

@Builder
public record ContributionResponseDto(

    String nickname,

    ProblemTierType tier,

    List<ProblemTagType> tags,

    String comment
) {

    public static ContributionResponseDto from(ContributionEntity c) {
        return ContributionResponseDto.builder()
            .nickname(c.userAccountEntity().nickname())
            .tier(c.tier())
            .tags(c.contributionTags().stream()
                .map(ContributionTagEntity::tag)
                .toList())
            .comment(c.comment())
            .build();
    }
}
