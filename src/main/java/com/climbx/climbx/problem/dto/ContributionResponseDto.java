package com.climbx.climbx.problem.dto;

import com.climbx.climbx.problem.entity.ContributionEntity;
import com.climbx.climbx.problem.entity.ContributionTagEntity;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.enums.UserTierType;
import java.util.List;
import lombok.Builder;

@Builder
public record ContributionResponseDto(

    String nickname,

    String profileImageCdnUrl,

    UserTierType userTier,

    ProblemTierType problemTier,

    List<ProblemTagType> tags,

    String comment
) {

    public static ContributionResponseDto from(ContributionEntity c) {
        UserAccountEntity user = c.userAccountEntity();
        return ContributionResponseDto.builder()
            .nickname(user.nickname())
            .profileImageCdnUrl(user.profileImageCdnUrl())
            .userTier(UserTierType.fromValue(user.userStatEntity().rating()))
            .problemTier(c.tier())
            .tags(c.contributionTags().stream()
                .map(ContributionTagEntity::tag)
                .toList())
            .comment(c.comment())
            .build();
    }
}
