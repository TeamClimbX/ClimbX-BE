package com.climbx.climbx.admin.user.service;

import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserStatRepository;
import com.climbx.climbx.user.service.UserDataAggregationService;
import com.climbx.climbx.user.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserLookupService userLookupService;
    private final UserStatRepository userStatRepository;
    private final UserDataAggregationService userDataAggregationService;

    @Transactional
    public UserProfileResponseDto updateRating(String nickname, Integer rating) {
        UserAccountEntity userAccount = userLookupService.findUserByNickname(nickname);

        Long userId = userAccount.userId();

        UserStatEntity userStat = userStatRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자 통계 정보를 찾을 수 없습니다: " + userId));

        userStat.setRating(rating);

        return userDataAggregationService.buildProfile(userAccount);
    }
}
