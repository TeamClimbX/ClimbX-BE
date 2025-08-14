package com.climbx.climbx.user.service;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserAnalyticsService {

    private final UserLookupService userLookupService;
    private final SubmissionRepository submissionRepository;
    private final UserRankingHistoryRepository userRankingHistoryRepository;

    public List<ProblemInfoResponseDto> getUserTopProblems(String nickname, Integer limit) {
        UserAccountEntity userAccount = userLookupService.findUserByNickname(nickname);
        Pageable pageable = PageRequest.of(0, limit);

        return submissionRepository.getUserTopProblems(
            userAccount.userId(),
            StatusType.ACCEPTED,
            pageable
        );
    }

    public List<DailyHistoryResponseDto> getUserStreak(
        String nickname,
        LocalDate from,
        LocalDate to
    ) {
        UserAccountEntity userAccount = userLookupService.findUserByNickname(nickname);

        return submissionRepository.getUserDateSolvedCount(
            userAccount.userId(),
            StatusType.ACCEPTED,
            from,
            to
        );
    }

    public List<DailyHistoryResponseDto> getUserDailyHistory(
        String nickname,
        CriteriaType criteria,
        LocalDate from,
        LocalDate to
    ) {
        UserAccountEntity userAccount = userLookupService.findUserByNickname(nickname);

        return userRankingHistoryRepository.getUserDailyHistory(
            userAccount.userId(),
            criteria,
            from,
            to
        );
    }
}