package com.climbx.climbx.user.dto;

import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.UserTierType;
import java.util.List;
import lombok.Builder;

@Builder
public record UserProfileResponseDto(

    String nickname,

    String statusMessage,

    String profileImageCdnUrl, // null 허용

    Integer ranking,

    RatingResponseDto rating,

    UserTierType tier,

    List<TagRatingResponseDto> categoryRatings,

    Integer currentStreak,

    Integer longestStreak,

    Integer solvedCount,

    Integer submissionCount,

    Integer contributionCount,

    Integer rivalCount
) {

    public static UserProfileResponseDto from(
        UserAccountEntity account,
        UserStatEntity stat,
        UserTierType tier,
        RatingResponseDto rating,
        Integer ranking,
        List<TagRatingResponseDto> categoryRatings
    ) {

        return UserProfileResponseDto.builder()
            .nickname(account.nickname())
            .statusMessage(account.statusMessage())
            .tier(tier)
            .profileImageCdnUrl(account.profileImageCdnUrl())
            .ranking(ranking)
            .rating(rating)
            .categoryRatings(categoryRatings)
            .currentStreak(stat.currentStreak())
            .longestStreak(stat.longestStreak())
            .solvedCount(stat.solvedCount())
            .submissionCount(stat.submissionCount())
            .contributionCount(stat.contributionCount())
            .rivalCount(stat.rivalCount())
            .build();
    }
}
