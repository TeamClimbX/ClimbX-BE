package com.climbx.climbx.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.dto.TagRatingPairDto;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.TagRatingResponseDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.UserTierType;
import com.climbx.climbx.user.exception.UserStatNotFoundException;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.util.UserRatingUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDataAggregationServiceTest {

    @Mock
    private UserStatRepository userStatRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRatingUtil userRatingUtil;

    @InjectMocks
    private UserDataAggregationService userDataAggregationService;

    private UserAccountEntity createMockUserAccountEntity(Long userId, String nickname) {
        return UserAccountEntity.builder()
            .userId(userId)
            .nickname(nickname)
            .role(RoleType.USER)
            .statusMessage("Test status")
            .profileImageCdnUrl("https://example.com/profile.jpg")
            .build();
    }

    private UserStatEntity createMockUserStatEntity(Integer rating, Integer topProblemRating) {
        UserStatEntity userStat = mock(UserStatEntity.class);
        given(userStat.rating()).willReturn(rating);
        given(userStat.topProblemRating()).willReturn(topProblemRating);
        given(userStat.currentStreak()).willReturn(5);
        given(userStat.longestStreak()).willReturn(10);
        given(userStat.solvedCount()).willReturn(50);
        given(userStat.submissionCount()).willReturn(75);
        given(userStat.contributionCount()).willReturn(3);
        given(userStat.rivalCount()).willReturn(2);
        given(userStat.updatedAt()).willReturn(LocalDateTime.now());
        return userStat;
    }

    @Nested
    @DisplayName("프로필 빌드")
    class BuildProfile {

        @Test
        @DisplayName("완전한 프로필 정보로 빌드 성공")
        void buildProfile_Success_CompleteData() {
            // given
            Long userId = 1L;
            String nickname = "alice";
            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            UserStatEntity userStat = createMockUserStatEntity(1500, 800);

            List<TagRatingPairDto> acceptedTags = List.of();
            List<TagRatingPairDto> allTags = List.of();
            List<TagRatingResponseDto> categoryRatings = List.of(
                TagRatingResponseDto.builder()
                    .category("Bouldering")
                    .rating(1400)
                    .build()
            );

            given(userStatRepository.findByUserId(userId)).willReturn(Optional.of(userStat));
            given(userStatRepository.findRankByRatingAndUpdatedAtAndUserId(eq(1500),
                any(LocalDateTime.class), eq(userId)))
                .willReturn(42);
            given(submissionRepository.getUserAcceptedSubmissionTagSummary(userId,
                StatusType.ACCEPTED))
                .willReturn(acceptedTags);
            given(submissionRepository.getUserAcceptedSubmissionTagSummary(userId, null))
                .willReturn(allTags);
            given(userRatingUtil.calculateCategoryRating(acceptedTags, allTags))
                .willReturn(categoryRatings);

            // when
            UserProfileResponseDto result = userDataAggregationService.buildProfile(user);

            // then
            assertThat(result.nickname()).isEqualTo(nickname);
            assertThat(result.tier()).isEqualTo(UserTierType.P2);
            assertThat(result.rating().totalRating()).isEqualTo(1500);
            assertThat(result.rating().topProblemRating()).isEqualTo(800);
            assertThat(result.ranking()).isEqualTo(42);
            assertThat(result.categoryRatings()).hasSize(1);
            assertThat(result.currentStreak()).isEqualTo(5);
            assertThat(result.longestStreak()).isEqualTo(10);
            assertThat(result.solvedCount()).isEqualTo(50);
            assertThat(result.submissionCount()).isEqualTo(75);
            assertThat(result.contributionCount()).isEqualTo(3);
            assertThat(result.rivalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("사용자 통계가 없는 경우 예외 발생")
        void buildProfile_UserStatNotFound() {
            // given
            Long userId = 1L;
            String nickname = "newUser";
            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);

            given(userStatRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userDataAggregationService.buildProfile(user))
                .isInstanceOf(UserStatNotFoundException.class);
        }

        @Test
        @DisplayName("높은 레이팅 사용자의 티어 계산")
        void buildProfile_Success_HighRating() {
            // given
            Long userId = 1L;
            String nickname = "proClimber";
            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            UserStatEntity userStat = createMockUserStatEntity(2500, 1500);

            given(userStatRepository.findByUserId(userId)).willReturn(Optional.of(userStat));
            given(userStatRepository.findRankByRatingAndUpdatedAtAndUserId(eq(2500),
                any(LocalDateTime.class), eq(userId)))
                .willReturn(1);
            given(submissionRepository.getUserAcceptedSubmissionTagSummary(userId,
                StatusType.ACCEPTED))
                .willReturn(List.of());
            given(submissionRepository.getUserAcceptedSubmissionTagSummary(userId, null))
                .willReturn(List.of());
            given(userRatingUtil.calculateCategoryRating(List.of(), List.of()))
                .willReturn(List.of());

            // when
            UserProfileResponseDto result = userDataAggregationService.buildProfile(user);

            // then
            assertThat(result.nickname()).isEqualTo(nickname);
            assertThat(result.tier()).isEqualTo(UserTierType.M);
            assertThat(result.rating().totalRating()).isEqualTo(2500);
            assertThat(result.rating().topProblemRating()).isEqualTo(1500);
            assertThat(result.ranking()).isEqualTo(1);
        }

        @Test
        @DisplayName("다양한 카테고리 레이팅이 있는 사용자")
        void buildProfile_Success_MultipleCategoryRatings() {
            // given
            Long userId = 1L;
            String nickname = "versatileClimber";
            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            UserStatEntity userStat = createMockUserStatEntity(1800, 1000);

            List<TagRatingResponseDto> categoryRatings = List.of(
                TagRatingResponseDto.builder()
                    .category("Bouldering")
                    .rating(1900)
                    .build(),
                TagRatingResponseDto.builder()
                    .category("Sport")
                    .rating(1700)
                    .build()
            );

            given(userStatRepository.findByUserId(userId)).willReturn(Optional.of(userStat));
            given(userStatRepository.findRankByRatingAndUpdatedAtAndUserId(eq(1800),
                any(LocalDateTime.class), eq(userId)))
                .willReturn(15);
            given(submissionRepository.getUserAcceptedSubmissionTagSummary(userId,
                StatusType.ACCEPTED))
                .willReturn(List.of());
            given(submissionRepository.getUserAcceptedSubmissionTagSummary(userId, null))
                .willReturn(List.of());
            given(userRatingUtil.calculateCategoryRating(List.of(), List.of()))
                .willReturn(categoryRatings);

            // when
            UserProfileResponseDto result = userDataAggregationService.buildProfile(user);

            // then
            assertThat(result.categoryRatings()).hasSize(2);
            assertThat(result.categoryRatings().get(0).category()).isEqualTo("Bouldering");
            assertThat(result.categoryRatings().get(0).rating()).isEqualTo(1900);
            assertThat(result.categoryRatings().get(1).category()).isEqualTo("Sport");
            assertThat(result.categoryRatings().get(1).rating()).isEqualTo(1700);
        }
    }
}