package com.climbx.climbx.problem.util;

import com.climbx.climbx.common.exception.EmptyVoteException;
import com.climbx.climbx.problem.dto.VoteTierDto;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProblemRatingUtil {

    /**
     * 문제의 난이도 티어를 계산합니다.
     *
     * @param voteTiers 투표 티어 목록(sorted by dateTime)
     * @return 문제의 난이도 티어
     */
    public Integer calculateProblemTier(List<VoteTierDto> voteTiers) {
        if (voteTiers.isEmpty()) {
            throw new EmptyVoteException();
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