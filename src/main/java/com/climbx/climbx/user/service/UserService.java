package com.climbx.climbx.user.service;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.common.util.RatingUtil;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.dto.TagRatingResponseDto;
import com.climbx.climbx.user.dto.UserProfileInfoModifyRequestDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.UserTierType;
import com.climbx.climbx.user.exception.DuplicateNicknameException;
import com.climbx.climbx.user.exception.NicknameMismatchException;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.exception.UserStatNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.repository.UserRankingHistoryRepository;
import com.climbx.climbx.user.repository.UserStatRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final UserStatRepository userStatRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRankingHistoryRepository userRankingHistoryRepository;
    private final S3Service s3Service;
    private final RatingUtil ratingUtil;

    public List<UserProfileResponseDto> getUsers(String search) {
        List<UserAccountEntity> userAccounts;

        if (search == null || search.trim().isEmpty()) {
            userAccounts = userAccountRepository.findByRole(RoleType.USER);
        } else {
            userAccounts = userAccountRepository.findByRoleAndNicknameContaining(
                RoleType.USER,
                search.trim()
            );
        }

        return userAccounts.stream()
            .map(this::buildProfile)
            .toList();
    }

    public UserProfileResponseDto getUserById(Long userId) {
        UserAccountEntity userAccountEntity = findUserById(userId);
        return buildProfile(userAccountEntity);
    }

    public UserProfileResponseDto getUserByNickname(String nickname) {
        UserAccountEntity userAccountEntity = findUserByNickname(nickname);
        return buildProfile(userAccountEntity);
    }

    @Transactional
    public UserProfileResponseDto modifyUserProfileInfo(
        Long userId,
        String currentNickname,
        UserProfileInfoModifyRequestDto requestDto
    ) {
        UserAccountEntity userAccountEntity = findUserById(userId);

        if (!currentNickname.equals(userAccountEntity.nickname())) {
            throw new NicknameMismatchException(currentNickname, userAccountEntity.nickname());
        }

        if (!currentNickname.equals(requestDto.newNickname())
            && userAccountRepository.existsByNickname(requestDto.newNickname())) {
            throw new DuplicateNicknameException(requestDto.newNickname());
        }

        userAccountEntity.modifyProfileInfo(
            requestDto.newNickname(),
            requestDto.newStatusMessage()
        );

        return buildProfile(userAccountEntity);
    }

    @Transactional
    public UserProfileResponseDto updateUserProfileImage(
        Long userId,
        String nickname,
        MultipartFile profileImage
    ) {
        UserAccountEntity userAccountEntity = findUserById(userId);

        if (!nickname.equals(userAccountEntity.nickname())) {
            throw new NicknameMismatchException(nickname, userAccountEntity.nickname());
        }

        // 프로필 이미지 업로드 처리
        // 입력 이미지가 없다면 null을 저장하여 기본 프로필 이미지로 설정
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3Service.uploadProfileImage(userId, profileImage);
        }

        log.info("프로필 이미지 URL: {}", profileImageUrl == null ? "기본 프로필 이미지(null)" : profileImageUrl);
        userAccountEntity.updateProfileImageUrl(profileImageUrl);

        return buildProfile(userAccountEntity);
    }

    public List<ProblemInfoResponseDto> getUserTopProblems(String nickname, Integer limit) {
        UserAccountEntity userAccount = findUserByNickname(nickname);
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
        UserAccountEntity userAccount = findUserByNickname(nickname);

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
        UserAccountEntity userAccount = findUserByNickname(nickname);

        return userRankingHistoryRepository.getUserDailyHistory(
            userAccount.userId(),
            criteria,
            from,
            to
        );
    }

    private UserProfileResponseDto buildProfile(UserAccountEntity userAccount) {
        Long userId = userAccount.userId();

        UserStatEntity userStat = findUserStatByUserId(userId);
        UserTierType tier = UserTierType.fromValue(userStat.rating());
        Integer ratingRank = userStatRepository.findRankByRatingAndUpdatedAtAndUserId(
            userStat.rating(), userStat.updatedAt(), userId);

        List<TagRatingResponseDto> categoryRatings = ratingUtil.calculateCategoryRating(
            submissionRepository.getUserAcceptedSubmissionTagSummary(userId, StatusType.ACCEPTED),
            submissionRepository.getUserAcceptedSubmissionTagSummary(userId, null)
        );

        return UserProfileResponseDto.from(
            userAccount,
            userStat,
            tier,
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
