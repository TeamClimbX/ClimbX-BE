package com.climbx.climbx.user;

import com.climbx.climbx.common.comcode.ComcodeService;
import com.climbx.climbx.problem.dto.ProblemDetailsResponseDto;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.dto.UserProfileModifyRequestDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.DuplicateNicknameException;
import com.climbx.climbx.user.exception.NicknameMismatchException;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.exception.UserStatNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class UserService {

    private final UserAccountRepository userAccountRepository;
    private final UserStatRepository userStatRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRankingHistoryRepository userRankingHistoryRepository;
    private final ComcodeService comcodeService;

    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getUsers(String search) {
        List<UserAccountEntity> userAccounts;

        String userRoleCode = comcodeService.getCodeValue("USER");

        if (search == null || search.trim().isEmpty()) {
            userAccounts = userAccountRepository.findByRole(userRoleCode);
        } else {
            userAccounts = userAccountRepository.findByRoleAndNicknameContaining(userRoleCode,
                search.trim());
        }

        return userAccounts.stream()
            .map(this::buildProfile)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserById(Long userId) {
        UserAccountEntity userAccountEntity = findUserById(userId);
        return buildProfile(userAccountEntity);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserByNickname(String nickname) {
        UserAccountEntity userAccountEntity = findUserByNickname(nickname);
        return buildProfile(userAccountEntity);
    }

    @Transactional
    public UserProfileResponseDto modifyUserProfile(
        Long userId,
        String currentNickname,
        UserProfileModifyRequestDto userProfileDto
    ) {
        UserAccountEntity userAccountEntity = findUserById(userId);

        if (!currentNickname.equals(userAccountEntity.nickname())) {
            throw new NicknameMismatchException(currentNickname, userAccountEntity.nickname());
        }

        if (!currentNickname.equals(userProfileDto.newNickname())
            && userAccountRepository.existsByNickname(userProfileDto.newNickname())) {
            throw new DuplicateNicknameException(userProfileDto.newNickname());
        }

        userAccountEntity.modifyProfile(
            userProfileDto.newNickname(),
            userProfileDto.newStatusMessage(),
            userProfileDto.newProfileImageUrl()
        );

        return buildProfile(userAccountEntity);
    }

    @Transactional(readOnly = true)
    public List<ProblemDetailsResponseDto> getUserTopProblems(String nickname, Integer limit) {
        UserAccountEntity userAccount = findUserByNickname(nickname);
        Sort sort = Sort.by("problemEntity.problemRating").descending();
        Pageable pageable = PageRequest.of(0, limit, sort);

        List<ProblemEntity> problemEntities = submissionRepository.getUserSubmissionProblems(
            userAccount.userId(),
            comcodeService.getCodeValue("ACCEPTED"),
            pageable
        );

        return problemEntities.stream()
            .map(ProblemDetailsResponseDto::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DailyHistoryResponseDto> getUserStreak(
        String nickname,
        LocalDate from,
        LocalDate to
    ) {
        UserAccountEntity userAccount = findUserByNickname(nickname);

        return submissionRepository.getUserDateSolvedCount(
            userAccount.userId(),
            comcodeService.getCodeValue("ACCEPTED"),
            from,
            to
        );
    }

    @Transactional(readOnly = true)
    public List<DailyHistoryResponseDto> getUserDailyHistory(
        String nickname,
        String criteria,
        LocalDate from,
        LocalDate to
    ) {
        UserAccountEntity userAccount = findUserByNickname(nickname);
        criteria = comcodeService.getCodeValue(criteria);

        return userRankingHistoryRepository.getUserDailyHistory(
            userAccount.userId(),
            criteria,
            from,
            to
        );
    }

    private UserProfileResponseDto buildProfile(UserAccountEntity userAccount) {
        UserStatEntity userStat = findUserStatByUserId(userAccount.userId());
        Long ratingRank = userStatRepository.findRatingRank(userStat.rating());
        Map<String, Long> categoryRatings = Collections.emptyMap();

        return UserProfileResponseDto.from(
            userAccount,
            userStat,
            ratingRank,
            categoryRatings
        );
    }

    private UserAccountEntity findUserById(Long userId) {
        return userAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserAccountEntity findUserByNickname(String nickname) {
        return userAccountRepository.findByNickname(nickname)
            .orElseThrow(() -> new UserNotFoundException(nickname));
    }

    protected UserStatEntity findUserStatByUserId(Long userId) {
        return userStatRepository.findByUserId(userId)
            .orElseThrow(() -> new UserStatNotFoundException(userId));
    }
}
