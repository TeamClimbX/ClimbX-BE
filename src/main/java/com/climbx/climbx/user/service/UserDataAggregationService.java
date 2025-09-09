package com.climbx.climbx.user.service;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.dto.TagRatingPairDto;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.RatingResponseDto;
import com.climbx.climbx.user.dto.TagRatingResponseDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.dto.UserRankingDto;
import com.climbx.climbx.user.dto.UserTagRatingDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.UserTierType;
import com.climbx.climbx.user.exception.UserStatNotFoundException;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.util.UserRatingUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDataAggregationService {

    private final UserStatRepository userStatRepository;
    private final SubmissionRepository submissionRepository;

    public UserProfileResponseDto buildProfile(UserAccountEntity userAccount) {
        Long userId = userAccount.userId();

        UserStatEntity userStat = findUserStatByUserId(userId);
        UserTierType tier = UserTierType.fromValue(userStat.rating());
        Integer ratingRank = userStatRepository.findRankByRatingAndUpdatedAtAndUserId(
            userStat.rating(), userStat.updatedAt(), userId);

        List<TagRatingResponseDto> categoryRatings = UserRatingUtil.calculateCategoryRating(
            submissionRepository.getUserAcceptedSubmissionTagSummary(userId, StatusType.ACCEPTED),
            submissionRepository.getUserAcceptedSubmissionTagSummary(userId, null)
        );

        Integer totalRating = userStat.rating();
        Integer topProblemRating = userStat.topProblemRating();
        Integer submissionRating = UserRatingUtil.calculateSubmissionScore(
            userStat.submissionCount());
        Integer solvedRating = UserRatingUtil.calculateSolvedScore(userStat.solvedCount());
        Integer contributionRating = UserRatingUtil.calculateContributionScore(
            userStat.contributionCount());

        RatingResponseDto rating = RatingResponseDto.builder()
            .totalRating(totalRating)
            .topProblemRating(topProblemRating)
            .submissionRating(submissionRating)
            .solvedRating(solvedRating)
            .contributionRating(contributionRating)
            .build();

        return UserProfileResponseDto.from(
            userAccount,
            userStat,
            tier,
            rating,
            ratingRank,
            categoryRatings
        );
    }

    public List<UserProfileResponseDto> buildProfilesBatch(List<UserAccountEntity> userAccounts) {
        if (userAccounts.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = userAccounts.stream()
            .map(UserAccountEntity::userId)
            .toList();

        // 배치 조회: UserStat 데이터
        Map<Long, UserStatEntity> userStatMap = userStatRepository.findByUserIdIn(userIds)
            .stream()
            .collect(Collectors.toMap(UserStatEntity::userId, Function.identity()));

        // 배치 조회: 랭킹 데이터
        Map<Long, Integer> rankingMap = userStatRepository.findRanksByUserIds(userIds)
            .stream()
            .collect(Collectors.toMap(
                UserRankingDto::userId,
                UserRankingDto::ranking
            ));

        // 배치 조회: 태그 요약 데이터
        Map<Long, List<TagRatingPairDto>> acceptedTagsMap = buildTagSummaryMap(userIds,
            StatusType.ACCEPTED);
        Map<Long, List<TagRatingPairDto>> allTagsMap = buildTagSummaryMap(userIds, null);

        // 프로필 조립
        return userAccounts.stream()
            .map(userAccount -> {
                Long userId = userAccount.userId();
                UserStatEntity userStat = userStatMap.get(userId);

                if (userStat == null) {
                    throw new UserStatNotFoundException(userId);
                }

                UserTierType tier = UserTierType.fromValue(userStat.rating());
                Integer ratingRank = rankingMap.get(userId);

                List<TagRatingResponseDto> categoryRatings = UserRatingUtil.calculateCategoryRating(
                    acceptedTagsMap.getOrDefault(userId, List.of()),
                    allTagsMap.getOrDefault(userId, List.of())
                );

                RatingResponseDto rating = buildRatingResponse(userStat);

                return UserProfileResponseDto.from(
                    userAccount,
                    userStat,
                    tier,
                    rating,
                    ratingRank,
                    categoryRatings
                );
            })
            .toList();
    }

    private Map<Long, List<TagRatingPairDto>> buildTagSummaryMap(List<Long> userIds,
        StatusType status) {
        List<UserTagRatingDto> primaryTags = submissionRepository.summarizeByPrimaryBatch(userIds,
            status);
        List<UserTagRatingDto> secondaryTags = submissionRepository.summarizeBySecondaryBatch(
            userIds, status);

        Map<Long, List<TagRatingPairDto>> tagMap = Stream.concat(
                primaryTags.stream(),
                secondaryTags.stream()
            )
            .collect(Collectors.groupingBy(
                UserTagRatingDto::userId,
                Collectors.mapping(
                    dto -> new TagRatingPairDto(dto.tag(), dto.rating()),
                    Collectors.toList()
                )
            ));

        return tagMap;
    }

    private RatingResponseDto buildRatingResponse(UserStatEntity userStat) {
        return RatingResponseDto.builder()
            .totalRating(userStat.rating())
            .topProblemRating(userStat.topProblemRating())
            .submissionRating(UserRatingUtil.calculateSubmissionScore(userStat.submissionCount()))
            .solvedRating(UserRatingUtil.calculateSolvedScore(userStat.solvedCount()))
            .contributionRating(
                UserRatingUtil.calculateContributionScore(userStat.contributionCount()))
            .build();
    }

    /**
     * 현재 UserStat 엔티티의 값들로 rating을 계산합니다. AdminSubmissionService에서 단순 계산이 필요할 때 사용됩니다.
     */
    public RatingResponseDto calculateUserRatingFromCurrentStats(UserStatEntity userStat) {
        return UserRatingUtil.calculateUserRating(
            userStat.topProblemRating(),
            userStat.submissionCount(),
            userStat.solvedCount(),
            userStat.contributionCount()
        );
    }

    /**
     * topProblemRating을 갱신한 후 전체 레이팅을 재계산하고 UserStat을 업데이트합니다. 배치 처리나 전체 재계산이 필요할 때 사용됩니다.
     */
    @Transactional
    public void recalculateAndUpdateUserRating(Long userId) {
        UserStatEntity userStat = findUserStatByUserId(userId);

        List<Integer> topProblemRatings = getUserTopProblemRatings(userId);

        // topProblemRating을 실제 해결한 문제들 중 최대값으로 갱신
        int actualTopProblemRating = topProblemRatings.stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0);

        userStat.setTopProblemRating(actualTopProblemRating);

        // 갱신된 값들로 rating 재계산 (단순 계산 메서드 호출)
        RatingResponseDto updatedRating = calculateUserRatingFromCurrentStats(userStat);

        userStat.setRating(updatedRating.totalRating());

        log.debug("Updated user rating for userId: {}, new rating: {}",
            userId, updatedRating.totalRating());
    }

    private List<Integer> getUserTopProblemRatings(Long userId) {
        return submissionRepository.getUserTopProblems(
                userId,
                StatusType.ACCEPTED,
                Pageable.ofSize(50)
            ).stream()
            .map(ProblemInfoResponseDto::rating)
            .toList();
    }

    protected UserStatEntity findUserStatByUserId(Long userId) {
        return userStatRepository.findByUserId(userId)
            .orElseThrow(() -> new UserStatNotFoundException(userId));
    }
}