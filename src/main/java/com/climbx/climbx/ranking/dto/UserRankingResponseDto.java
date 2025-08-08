package com.climbx.climbx.ranking.dto;

import com.climbx.climbx.user.entity.UserStatEntity;
import lombok.Builder;

@Builder
public record UserRankingResponseDto(

    String nickname,
    String statusMessage,
    String profileImageCdnUrl, // null 허용
    Integer rating,
    Long ranking,
    Integer currentStreak,
    Integer longestStreak,
    Integer solvedCount
) {

    public static UserRankingResponseDto from(UserStatEntity user, Long ranking) {
        return UserRankingResponseDto.builder()
            .nickname(user.userAccountEntity().nickname())
            .statusMessage(user.userAccountEntity().statusMessage())
            .profileImageCdnUrl(user.userAccountEntity().profileImageCdnUrl())
            .ranking(ranking)
            .rating(user.rating())
            .currentStreak(user.currentStreak())
            .longestStreak(user.longestStreak())
            .solvedCount(user.solvedCount())
            .build();
    }
}
