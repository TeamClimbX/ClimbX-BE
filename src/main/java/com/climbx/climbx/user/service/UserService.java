package com.climbx.climbx.user.service;

import com.climbx.climbx.common.enums.CriteriaType;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.dto.UserProfileInfoModifyRequestDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserProfileService userProfileService;
    private final UserAnalyticsService userAnalyticsService;

    public List<UserProfileResponseDto> getUsers(String search) {
        return userProfileService.getUsers(search);
    }

    public UserProfileResponseDto getUserById(Long userId) {
        return userProfileService.getUserById(userId);
    }

    public UserProfileResponseDto getUserByNickname(String nickname) {
        return userProfileService.getUserByNickname(nickname);
    }

    @Transactional
    public UserProfileResponseDto modifyUserProfileInfo(
        Long userId,
        String currentNickname,
        UserProfileInfoModifyRequestDto requestDto
    ) {
        return userProfileService.modifyUserProfileInfo(userId, currentNickname, requestDto);
    }

    @Transactional
    public UserProfileResponseDto updateUserProfileImage(
        Long userId,
        String nickname,
        MultipartFile profileImage
    ) {
        return userProfileService.updateUserProfileImage(userId, nickname, profileImage);
    }

    public List<ProblemInfoResponseDto> getUserTopProblems(String nickname, Integer limit) {
        return userAnalyticsService.getUserTopProblems(nickname, limit);
    }

    public List<DailyHistoryResponseDto> getUserStreak(
        String nickname,
        LocalDate from,
        LocalDate to
    ) {
        return userAnalyticsService.getUserStreak(nickname, from, to);
    }

    public List<DailyHistoryResponseDto> getUserDailyHistory(
        String nickname,
        CriteriaType criteria,
        LocalDate from,
        LocalDate to
    ) {
        return userAnalyticsService.getUserDailyHistory(nickname, criteria, from, to);
    }

}
