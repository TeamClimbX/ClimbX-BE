package com.climbx.climbx.common.util;

import com.climbx.climbx.problem.dto.TagRatingPairDto;
import com.climbx.climbx.problem.dto.VoteTierDto;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.user.dto.TagRatingResponseDto;
import java.time.Duration;
import java.time.LocalDateTime;
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
public class RatingUtil {

    static final int CATEGORY_TYPE_LIMIT = 8;

    public int calculateUserRating(
        List<Integer> topProblemRatings,
        int submissionCount,
        int solvedCount,
        int contributionCount
    ) {
        int topProblemScore = topProblemRatings.stream()
            .mapToInt(Integer::intValue)
            .map(rating -> ProblemTierType.fromValue(rating).value())
            .sum();

        int submissionCountScore = 10 * Math.min(submissionCount, 50);

        int solvedCountScore = (int) Math.round(1000 * (1 - Math.pow(0.98, solvedCount)));

        int contributionScore = (int) Math.round(100 * (1 - Math.pow(0.9, contributionCount)));

        return topProblemScore + submissionCountScore + solvedCountScore + contributionScore;
    }

    public List<TagRatingResponseDto> calculateCategoryRating(
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

        return allByTag.keySet().stream()
            .map(tag -> {
                List<Integer> solvedRatings = solvedByTag.getOrDefault(tag, List.of());
                List<Integer> allRatings = allByTag.getOrDefault(tag, List.of());

                int topProblemScore = 2 * solvedRatings.stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(50)
                    .mapToInt(Integer::intValue)
                    .sum();
                int allSubmissionScore = 10 * Math.min(allRatings.size(), 50);
                int solvedCountScore = (int) Math.round(
                    1000 * (1 - Math.pow(0.98, solvedRatings.size()))
                );
                return TagRatingResponseDto.builder()
                    .category(tag.displayName())
                    .rating(
                        topProblemScore + allSubmissionScore + solvedCountScore
                    ).build();
            }).sorted(Comparator.comparing(TagRatingResponseDto::rating).reversed())
            .toList()
            .subList(0, Math.min(CATEGORY_TYPE_LIMIT, allByTag.size()));
    }

    /**
     * 문제의 난이도 티어를 계산합니다.
     *
     * @param voteTiers 투표 티어 목록(sorted by dateTime)
     * @return 문제의 난이도 티어
     */
    public Integer calculateProblemTier(List<VoteTierDto> voteTiers) {
        if (voteTiers.isEmpty()) {
            // TODO: 암장의 난이도 분포에 따른 기본 티어 설정 로직 필요
            return ProblemTierType.B3.value();
        }

        LocalDateTime lastVoteTime = voteTiers.getLast().dateTime();
        int lastVoteIndex = voteTiers.size() - 1;

        double weightedTierSum = 0.0;
        double weightSum = 0.0;
        for (int i = 0; i < voteTiers.size(); i++) {
            VoteTierDto vote = voteTiers.get(i);

            double indexDiffReliability = Math.pow(0.9, lastVoteIndex - i);
            double dateDiffReliability = Math.pow(0.5, Duration.between(
                vote.dateTime(), lastVoteTime).toDays());

            double weight = Math.max(indexDiffReliability, dateDiffReliability);

            weightedTierSum += vote.tier().value() * weight;
            weightSum += weight;
        }

        return (int) Math.round(weightedTierSum / weightSum);
    }
}
