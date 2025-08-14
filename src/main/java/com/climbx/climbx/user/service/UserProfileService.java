package com.climbx.climbx.user.service;

import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.user.dto.UserProfileInfoModifyRequestDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.exception.DuplicateNicknameException;
import com.climbx.climbx.user.exception.NicknameMismatchException;
import com.climbx.climbx.user.repository.UserAccountRepository;
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
public class UserProfileService {

    private final UserAccountRepository userAccountRepository;
    private final UserLookupService userLookupService;
    private final S3Service s3Service;
    private final UserDataAggregationService userDataAggregationService;

    public List<UserProfileResponseDto> getUsers(String search) {
        List<UserAccountEntity> users;

        if (search == null || search.trim().isEmpty()) {
            users = userLookupService.findAllUsers();
        } else {
            users = userLookupService.findUsersByNicknameContaining(search.trim());
        }

        return userDataAggregationService.buildProfilesBatch(users);
    }

    public UserProfileResponseDto getUserById(Long userId) {
        UserAccountEntity userAccountEntity = userLookupService.findUserById(userId);
        return userDataAggregationService.buildProfile(userAccountEntity);
    }

    public UserProfileResponseDto getUserByNickname(String nickname) {
        UserAccountEntity userAccountEntity = userLookupService.findUserByNickname(nickname);
        return userDataAggregationService.buildProfile(userAccountEntity);
    }

    @Transactional
    public UserProfileResponseDto modifyUserProfileInfo(
        Long userId,
        String currentNickname,
        UserProfileInfoModifyRequestDto requestDto
    ) {
        UserAccountEntity userAccountEntity = userLookupService.findUserById(userId);

        if (!currentNickname.equals(userAccountEntity.nickname())) {
            throw new NicknameMismatchException(currentNickname, userAccountEntity.nickname());
        }

        if (!currentNickname.equals(requestDto.newNickname())
            && userAccountRepository.existsByNicknameIgnoringRole(requestDto.newNickname())) {
            throw new DuplicateNicknameException(requestDto.newNickname());
        }

        userAccountEntity.modifyProfileInfo(
            requestDto.newNickname(),
            requestDto.newStatusMessage()
        );

        return userDataAggregationService.buildProfile(userAccountEntity);
    }

    @Transactional
    public UserProfileResponseDto updateUserProfileImage(
        Long userId,
        String nickname,
        MultipartFile profileImage
    ) {
        UserAccountEntity userAccountEntity = userLookupService.findUserById(userId);

        if (!nickname.equals(userAccountEntity.nickname())) {
            throw new NicknameMismatchException(nickname, userAccountEntity.nickname());
        }

        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3Service.uploadProfileImage(userId, profileImage);
        }

        log.info("프로필 이미지 URL: {}", profileImageUrl == null ? "기본 프로필 이미지(null)" : profileImageUrl);
        userAccountEntity.updateProfileImageUrl(profileImageUrl);

        return userDataAggregationService.buildProfile(userAccountEntity);
    }

}