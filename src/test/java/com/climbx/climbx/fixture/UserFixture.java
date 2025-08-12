package com.climbx.climbx.fixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.dto.RatingResponseDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.UserTierType;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.util.UserRatingUtil;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
            .rating(
                RatingResponseDto.builder()
                    .totalRating(rating)
                    .topProblemRating(0)
                    .submissionRating(0)
                    .solvedRating(
                        UserRatingUtil.calculateSolvedScore(DEFAULT_SOLVED_PROBLEMS_COUNT))
                    .contributionRating(0)
                    .build()
            )
            .tier(UserTierType.fromValue(rating))
            .categoryRatings(Collections.emptyList())
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
            .rating(
                RatingResponseDto.builder()
                    .totalRating(rating)
                    .topProblemRating(0)
                    .submissionRating(0)
                    .solvedRating(UserRatingUtil.calculateSolvedScore(solvedProblemsCount))
                    .contributionRating(0)
                    .build()
            )
            .tier(UserTierType.fromValue(rating))
            .categoryRatings(Collections.emptyList())
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .solvedCount(solvedProblemsCount)
            .submissionCount(0)
            .contributionCount(0)
            .rivalCount(rivalCount)
            .build();
    }

    // Stubbing helpers to reduce repetition in tests
    public static UserStatEntity stubUserStatAndRank(
        UserStatRepository userStatRepository,
        Long userId,
        Integer rating,
        Integer expectedRank
    ) {
        UserStatEntity stat = createUserStatEntity(userId, rating);
        given(userStatRepository.findByUserId(userId))
            .willReturn(Optional.of(stat));
        given(
            userStatRepository.findRankByRatingAndUpdatedAtAndUserId(rating, stat.updatedAt(),
                userId)
        ).willReturn(expectedRank);
        return stat;
    }

    public static UserStatEntity stubUserStatAndRank(
        UserStatRepository userStatRepository,
        Long userId
    ) {
        return stubUserStatAndRank(userStatRepository, userId, DEFAULT_RATING, DEFAULT_RANKING);
    }

    public static UserStatEntity stubUserStatAndRank(
        UserStatRepository userStatRepository,
        UserStatEntity userStatEntity,
        Integer expectedRank
    ) {
        Long userId = userStatEntity.userId();
        Integer rating = userStatEntity.rating();
        given(userStatRepository.findByUserId(userId))
            .willReturn(Optional.of(userStatEntity));
        given(
            userStatRepository.findRankByRatingAndUpdatedAtAndUserId(rating,
                userStatEntity.updatedAt(), userId)
        ).willReturn(expectedRank);
        return userStatEntity;
    }

    // Batch stubbing helper for user stats and ranks
    public static void stubStatsFor(
        UserStatRepository userStatRepository,
        List<UserAccountEntity> accounts,
        int[] ratings,
        int[] ranks
    ) {
        for (int i = 0; i < accounts.size(); i++) {
            Long userId = accounts.get(i).userId();
            int rating = ratings.length > i ? ratings[i] : DEFAULT_RATING;
            int rank = ranks.length > i ? ranks[i] : DEFAULT_RANKING;
            UserStatEntity stat = createUserStatEntity(userId, rating);
            given(userStatRepository.findByUserId(userId)).willReturn(Optional.of(stat));
            given(userStatRepository.findRankByRatingAndUpdatedAtAndUserId(rating, stat.updatedAt(),
                userId))
                .willReturn(rank);
        }
    }

    public static void stubStatsFor(
        UserStatRepository userStatRepository,
        List<UserAccountEntity> accounts
    ) {
        stubStatsFor(userStatRepository, accounts, new int[0], new int[0]);
    }

    // Assertion helper for user profile
    public static void assertUserProfile(
        UserProfileResponseDto actual,
        String expectedNickname,
        int expectedRating,
        int expectedRanking
    ) {
        assertThat(actual.nickname()).isEqualTo(expectedNickname);
        assertThat(actual.rating().totalRating()).isEqualTo(expectedRating);
        assertThat(actual.ranking()).isEqualTo(expectedRanking);
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

    // 예외 테스트를 위한 간단한 헬퍼 메서드들
    public static void stubUserNotFound(
        com.climbx.climbx.user.repository.UserAccountRepository userAccountRepository,
        Long userId
    ) {
        given(userAccountRepository.findByUserId(userId)).willReturn(Optional.empty());
    }

    public static void stubUserNotFoundByNickname(
        com.climbx.climbx.user.repository.UserAccountRepository userAccountRepository,
        String nickname
    ) {
        given(userAccountRepository.findByNickname(nickname)).willReturn(Optional.empty());
    }

    public static void stubUserStatNotFound(UserStatRepository userStatRepository, Long userId) {
        given(userStatRepository.findByUserId(userId)).willReturn(Optional.empty());
    }
} 