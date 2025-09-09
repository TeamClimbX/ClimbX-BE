package com.climbx.climbx.user.util;

import com.climbx.climbx.problem.dto.TagRatingPairDto;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.user.dto.RatingResponseDto;
import com.climbx.climbx.user.dto.TagRatingResponseDto;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRatingUtil {

    static final int CATEGORY_TYPE_LIMIT = 8;

    public static int calculateSubmissionScore(int submissionCount) {
        return 10 * Math.min(submissionCount, 50);
    }

    public static int calculateSolvedScore(int solvedCount) {
        return (int) Math.round(1000 * (1 - Math.pow(0.98, solvedCount)));
    }

    public static int calculateContributionScore(int contributionCount) {
        return (int) Math.round(100 * (1 - Math.pow(0.9, contributionCount)));
    }

    public static RatingResponseDto calculateUserRating(
        int topProblemScore,
        int submissionCount,
        int solvedCount,
        int contributionCount
    ) {
        int submissionCountScore = calculateSubmissionScore(submissionCount);

        int solvedCountScore = calculateSolvedScore(solvedCount);

        int contributionScore = calculateContributionScore(contributionCount);

        int totalRating =
            topProblemScore + submissionCountScore + solvedCountScore + contributionScore;

        return RatingResponseDto.builder()
            .totalRating(totalRating)
            .topProblemRating(topProblemScore)
            .submissionRating(submissionCountScore)
            .solvedRating(solvedCountScore)
            .contributionRating(contributionScore)
            .build();
    }

    public static List<TagRatingResponseDto> calculateCategoryRating(
        List<TagRatingPairDto> solvedTags,
        List<TagRatingPairDto> allTags
    ) {
        // {Problem, tag} 형태로 solvedTags와 allTags를 받음.
        // Problem은 각각 2개씩 있을 수 있음

        log.info("Calculating category ratings for {} solved tags and {} all tags",
            solvedTags.size(), allTags.size());
        solvedTags.forEach(tag -> log.debug("Solved tag: {}, rating: {}",
            tag.tag(), tag.rating()));
        allTags.forEach(tag -> log.debug("All tag: {}, rating: {}",
            tag.tag(), tag.rating()));

        Map<ProblemTagType, List<Integer>> solvedByTag = solvedTags.stream()
            .collect(Collectors.groupingBy(
                TagRatingPairDto::tag,
                Collectors.mapping(TagRatingPairDto::rating, Collectors.toList())
            ));

        Map<ProblemTagType, List<Integer>> allByTag = allTags.stream()
            .collect(Collectors.groupingBy(
                TagRatingPairDto::tag,
                Collectors.mapping(TagRatingPairDto::rating, Collectors.toList())
            ));

        record TagRatingWithPriority(TagRatingResponseDto dto, int priority) {

        }

        List<TagRatingResponseDto> allCategoryRatings = Arrays.stream(ProblemTagType.values())
            .map(tag -> {
                List<Integer> solvedRatings = solvedByTag.getOrDefault(tag, List.of());
                List<Integer> allRatings = allByTag.getOrDefault(tag, List.of());

                int topProblemScore = 2 * solvedRatings.stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(50)
                    .mapToInt(Integer::intValue)
                    .map(rating -> ProblemTierType.fromValue(rating).value())
                    .sum();
                int allSubmissionScore = 10 * Math.min(allRatings.size(), 50);
                int solvedCountScore = (int) Math.round(
                    1000 * (1 - Math.pow(0.98, solvedRatings.size()))
                );
                TagRatingResponseDto dto = TagRatingResponseDto.builder()
                    .category(tag.displayName())
                    .rating(
                        topProblemScore + allSubmissionScore + solvedCountScore
                    )
                    .build();
                return new TagRatingWithPriority(dto, tag.priority());
            }).sorted(Comparator.comparing((TagRatingWithPriority t) -> t.dto().rating()).reversed()
                .thenComparing(t -> t.priority()))
            .map(TagRatingWithPriority::dto)
            .toList();

        return allCategoryRatings.subList(0,
            Math.min(CATEGORY_TYPE_LIMIT, allCategoryRatings.size()));
    }
}