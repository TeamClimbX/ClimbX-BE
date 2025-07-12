package com.climbx.climbx.fixture;

import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import java.time.LocalDate;
import java.util.Collections;

public class UserFixture {

    public static final String DEFAULT_NICKNAME = "testUser";
    public static final String DEFAULT_STATUS_MESSAGE = "Test status message";
    public static final String DEFAULT_PROFILE_IMAGE_URL = "http://example.com/profile.jpg";
    public static final Long DEFAULT_RATING = 1500L;
    public static final Long DEFAULT_CURRENT_STREAK = 5L;
    public static final Long DEFAULT_LONGEST_STREAK = 15L;
    public static final Long DEFAULT_SOLVED_PROBLEMS_COUNT = 25L;
    public static final Long DEFAULT_RIVAL_COUNT = 3L;
    public static final Long DEFAULT_RANKING = 10L;

    // 편의 메서드들 (createUser 형태)
    public static UserAccountEntity createUser() {
        return createUserAccountEntity(1L);
    }

    public static UserAccountEntity createUser(String email, String nickname) {
        return UserAccountEntity.builder()
            .userId(1L)
            .nickname(nickname)
            .email(email)
            .statusMessage(DEFAULT_STATUS_MESSAGE)
            .profileImageUrl(DEFAULT_PROFILE_IMAGE_URL)
            .role("USER")
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
        return UserAccountEntity.builder()
            .userId(userId)
            .nickname(nickname)
            .statusMessage(statusMessage)
            .profileImageUrl(profileImageUrl)
            .role(role)
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

    public static UserStatEntity createUserStatEntity(Long userId, Long rating) {
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
        Long rating,
        Long currentStreak,
        Long longestStreak,
        Long solvedProblemsCount,
        Long rivalCount
    ) {
        return UserStatEntity.builder()
            .userId(userId)
            .rating(rating)
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .solvedProblemsCount(solvedProblemsCount)
            .rivalCount(rivalCount)
            .build();
    }

    // UserProfileResponseDto 생성 메서드
    public static UserProfileResponseDto createUserProfileResponseDto(
        String nickname,
        Long ranking
    ) {
        return createUserProfileResponseDto(nickname, ranking, DEFAULT_RATING);
    }

    public static UserProfileResponseDto createUserProfileResponseDto(
        String nickname,
        Long ranking,
        Long rating
    ) {
        return UserProfileResponseDto.builder()
            .nickname(nickname)
            .statusMessage(DEFAULT_STATUS_MESSAGE)
            .profileImageUrl(DEFAULT_PROFILE_IMAGE_URL)
            .ranking(ranking)
            .rating(rating)
            .categoryRatings(Collections.emptyMap())
            .currentStreak(DEFAULT_CURRENT_STREAK)
            .longestStreak(DEFAULT_LONGEST_STREAK)
            .solvedProblemsCount(DEFAULT_SOLVED_PROBLEMS_COUNT)
            .rivalCount(DEFAULT_RIVAL_COUNT)
            .build();
    }

    public static UserProfileResponseDto createUserProfileResponseDto(
        String nickname,
        String statusMessage,
        String profileImageUrl,
        Long ranking,
        Long rating,
        Long currentStreak,
        Long longestStreak,
        Long solvedProblemsCount,
        Long rivalCount
    ) {
        return UserProfileResponseDto.builder()
            .nickname(nickname)
            .statusMessage(statusMessage)
            .profileImageUrl(profileImageUrl)
            .ranking(ranking)
            .rating(rating)
            .categoryRatings(Collections.emptyMap())
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .solvedProblemsCount(solvedProblemsCount)
            .rivalCount(rivalCount)
            .build();
    }

    // DailyHistoryResponseDto 생성 메서드들
    public static DailyHistoryResponseDto createDailyHistoryResponseDto(
        LocalDate date,
        Long value
    ) {
        return DailyHistoryResponseDto.builder()
            .date(date)
            .value(value)
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
        String part,
        Long value
    ) {
        return UserRankingHistoryEntity.builder()
            .historyId(historyId)
            .userId(userId)
            .part(part)
            .value(value)
            .build();
    }

    public static UserRankingHistoryEntity createRatingHistoryEntity(
        Long historyId,
        Long userId,
        Long value
    ) {
        return createUserRankingHistoryEntity(
            historyId, userId, "RATING", value);
    }

    public static UserRankingHistoryEntity createRankingHistoryEntity(
        Long historyId,
        Long userId,
        Long value
    ) {
        return createUserRankingHistoryEntity(
            historyId, userId, "RANKING", value
        );
    }

    public static UserRankingHistoryEntity createSolvedCountHistoryEntity(
        Long historyId,
        Long userId,
        Long value
    ) {
        return createUserRankingHistoryEntity(
            historyId, userId, "SOLVED_COUNT", value
        );
    }
} 