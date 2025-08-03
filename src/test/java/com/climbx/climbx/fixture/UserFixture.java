package com.climbx.climbx.fixture;

import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.CriteriaType;
import java.time.LocalDate;
import java.util.Collections;

public class UserFixture {

    public static final String DEFAULT_NICKNAME = "testUser";
    public static final String DEFAULT_STATUS_MESSAGE = "Test activeStatus message";
    public static final String DEFAULT_PROFILE_IMAGE_URL = "http://example.com/profile.jpg";
    public static final Integer DEFAULT_RATING = 1500;
    public static final Integer DEFAULT_CURRENT_STREAK = 5;
    public static final Integer DEFAULT_LONGEST_STREAK = 15;
    public static final Integer DEFAULT_SOLVED_PROBLEMS_COUNT = 25;
    public static final Integer DEFAULT_RIVAL_COUNT = 3;
    public static final Integer DEFAULT_RANKING = 10;

    // 편의 메서드들 (createUser 형태)
    public static UserAccountEntity createUser() {
        return createUserAccountEntity(1L);
    }

    public static UserAccountEntity createUser(String email, String nickname) {
        return UserAccountEntity.builder()
            .userId(1L)
            .nickname(nickname)
            .statusMessage(DEFAULT_STATUS_MESSAGE)
            .profileImageCdnUrl(DEFAULT_PROFILE_IMAGE_URL)
            .role(RoleType.USER)
            .build();
    }

    public static UserAccountEntity createUser(Long userId) {
        return createUserAccountEntity(userId);
    }

    // UserAccountEntity 생성 메서드들
    public static UserAccountEntity createUserAccountEntity(Long userId) {
        return createUserAccountEntity(userId, DEFAULT_NICKNAME);
    }

    public static UserAccountEntity createUserAccountEntity(Long userId, String nickname) {
        return createUserAccountEntity(
            userId,
            nickname,
            DEFAULT_STATUS_MESSAGE,
            DEFAULT_PROFILE_IMAGE_URL
        );
    }

    public static UserAccountEntity createUserAccountEntity(Long userId, String nickname,
        String statusMessage, String profileImageUrl) {
        return createUserAccountEntity(
            userId,
            nickname,
            statusMessage,
            profileImageUrl,
            "USER"
        );
    }

    public static UserAccountEntity createUserAccountEntity(
        Long userId,
        String nickname,
        String statusMessage,
        String profileImageUrl,
        String role
    ) {
        UserStatEntity userStat = createUserStatEntity(userId);
        return UserAccountEntity.builder()
            .userId(userId)
            .nickname(nickname)
            .statusMessage(statusMessage)
            .profileImageCdnUrl(profileImageUrl)
            .role(RoleType.valueOf(role))
            .userStatEntity(userStat)
            .build();
    }

    // UserAccountEntity와 UserStatEntity를 함께 생성하는 메서드
    public static UserAccountEntity createUserAccountEntityWithStats(Long userId, String nickname) {
        UserStatEntity userStat = createUserStatEntity(userId);
        UserAccountEntity user = createUserAccountEntity(userId, nickname);

        // Builder 패턴을 사용하여 userStatEntity 설정
        return UserAccountEntity.builder()
            .userId(user.userId())
            .nickname(user.nickname())
            .statusMessage(user.statusMessage())
            .profileImageCdnUrl(user.profileImageCdnUrl())
            .role(user.role())
            .userStatEntity(userStat)
            .build();
    }

    public static UserAccountEntity createAdminUserAccountEntity(Long userId, String nickname) {
        return createUserAccountEntity(
            userId,
            nickname,
            DEFAULT_STATUS_MESSAGE,
            DEFAULT_PROFILE_IMAGE_URL,
            "ADMIN"
        );
    }

    // UserStatEntity 생성 메서드들  
    public static UserStatEntity createUserStatEntity(Long userId) {
        return createUserStatEntity(userId, DEFAULT_RATING);
    }

    public static UserStatEntity createUserStatEntity(Long userId, Integer rating) {
        return createUserStatEntity(
            userId,
            rating,
            DEFAULT_CURRENT_STREAK,
            DEFAULT_LONGEST_STREAK,
            DEFAULT_SOLVED_PROBLEMS_COUNT,
            DEFAULT_RIVAL_COUNT
        );
    }

    public static UserStatEntity createUserStatEntity(
        Long userId,
        Integer rating,
        Integer currentStreak,
        Integer longestStreak,
        Integer solvedProblemsCount,
        Integer rivalCount
    ) {
        return UserStatEntity.builder()
            .userId(userId)
            .rating(rating)
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .solvedCount(solvedProblemsCount)
            .submissionCount(0)
            .contributionCount(0)
            .rivalCount(rivalCount)
            .build();
    }

    // UserProfileResponseDto 생성 메서드
    public static UserProfileResponseDto createUserProfileResponseDto(
        String nickname,
        Integer ranking
    ) {
        return createUserProfileResponseDto(nickname, ranking, DEFAULT_RATING);
    }

    public static UserProfileResponseDto createUserProfileResponseDto(
        String nickname,
        Integer ranking,
        Integer rating
    ) {
        return UserProfileResponseDto.builder()
            .nickname(nickname)
            .statusMessage(DEFAULT_STATUS_MESSAGE)
            .profileImageCdnUrl(DEFAULT_PROFILE_IMAGE_URL)
            .ranking(ranking)
            .rating(rating)
            .tier("BRONZE1")
            .categoryRatings(Collections.emptyMap())
            .currentStreak(DEFAULT_CURRENT_STREAK)
            .longestStreak(DEFAULT_LONGEST_STREAK)
            .solvedCount(DEFAULT_SOLVED_PROBLEMS_COUNT)
            .submissionCount(0)
            .contributionCount(0)
            .rivalCount(DEFAULT_RIVAL_COUNT)
            .build();
    }

    public static UserProfileResponseDto createUserProfileResponseDto(
        String nickname,
        String statusMessage,
        String profileImageUrl,
        Integer ranking,
        Integer rating,
        Integer currentStreak,
        Integer longestStreak,
        Integer solvedProblemsCount,
        Integer rivalCount
    ) {
        return UserProfileResponseDto.builder()
            .nickname(nickname)
            .statusMessage(statusMessage)
            .profileImageCdnUrl(profileImageUrl)
            .ranking(ranking)
            .rating(rating)
            .tier("SILVER1")
            .categoryRatings(Collections.emptyMap())
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .solvedCount(solvedProblemsCount)
            .submissionCount(0)
            .contributionCount(0)
            .rivalCount(rivalCount)
            .build();
    }

    // DailyHistoryResponseDto 생성 메서드들
    public static DailyHistoryResponseDto createDailyHistoryResponseDto(
        LocalDate date,
        Integer value
    ) {
        return DailyHistoryResponseDto.builder()
            .date(date)
            .value((long) value)
            .build();
    }

    public static DailyHistoryResponseDto createRatingHistoryResponseDto(LocalDate date) {
        return createDailyHistoryResponseDto(date, DEFAULT_RATING);
    }

    public static DailyHistoryResponseDto createRankingHistoryResponseDto(LocalDate date) {
        return createDailyHistoryResponseDto(date, DEFAULT_RANKING);
    }

    public static DailyHistoryResponseDto createSolvedCountHistoryResponseDto(LocalDate date) {
        return createDailyHistoryResponseDto(date, DEFAULT_SOLVED_PROBLEMS_COUNT);
    }

    // UserRankingHistoryEntity 생성 메서드들
    public static UserRankingHistoryEntity createUserRankingHistoryEntity(
        Long historyId,
        Long userId,
        String criteria,
        Integer value
    ) {
        return UserRankingHistoryEntity.builder()
            .historyId(historyId)
            .userId(userId)
            .criteria(CriteriaType.from(criteria))
            .value(value)
            .build();
    }

    public static UserRankingHistoryEntity createRatingHistoryEntity(
        Long historyId,
        Long userId,
        Integer value
    ) {
        return createUserRankingHistoryEntity(
            historyId, userId, "RATING", value);
    }

    public static UserRankingHistoryEntity createRankingHistoryEntity(
        Long historyId,
        Long userId,
        Integer value
    ) {
        return createUserRankingHistoryEntity(
            historyId, userId, "RANKING", value
        );
    }

    public static UserRankingHistoryEntity createSolvedCountHistoryEntity(
        Long historyId,
        Long userId,
        Integer value
    ) {
        return createUserRankingHistoryEntity(
            historyId, userId, "SOLVED_COUNT", value
        );
    }
} 