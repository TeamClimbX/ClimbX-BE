package com.climbx.climbx.user.service;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.RatingResponseDto;
import com.climbx.climbx.user.dto.TagRatingResponseDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.UserTierType;
import com.climbx.climbx.user.exception.UserStatNotFoundException;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.util.UserRatingUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDataAggregationService {

    private final UserStatRepository userStatRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRatingUtil userRatingUtil;

    public UserProfileResponseDto buildProfile(UserAccountEntity userAccount) {
        Long userId = userAccount.userId();

        UserStatEntity userStat = findUserStatByUserId(userId);
        UserTierType tier = UserTierType.fromValue(userStat.rating());
        Integer ratingRank = userStatRepository.findRankByRatingAndUpdatedAtAndUserId(
            userStat.rating(), userStat.updatedAt(), userId);

        List<TagRatingResponseDto> categoryRatings = userRatingUtil.calculateCategoryRating(
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

    protected UserStatEntity findUserStatByUserId(Long userId) {
        return userStatRepository.findByUserId(userId)
            .orElseThrow(() -> new UserStatNotFoundException(userId));
    }
}