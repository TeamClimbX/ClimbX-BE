package com.climbx.climbx.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.user.dto.UserProfileInfoModifyRequestDto;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.enums.UserTierType;
import com.climbx.climbx.user.exception.DuplicateNicknameException;
import com.climbx.climbx.user.exception.NicknameMismatchException;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserLookupService userLookupService;

    @Mock
    private S3Service s3Service;

    @Mock
    private UserDataAggregationService userDataAggregationService;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserAccountEntity createMockUserEntity(Long userId, String nickname) {
        return UserAccountEntity.builder()
            .userId(userId)
            .nickname(nickname)
            .role(RoleType.USER)
            .build();
    }

    private UserAccountEntity createMockUserAccountEntity(Long userId, String nickname) {
        return UserAccountEntity.builder()
            .userId(userId)
            .nickname(nickname)
            .role(RoleType.USER)
            .build();
    }

    private UserProfileResponseDto createMockUserProfileResponseDto(String nickname) {
        return UserProfileResponseDto.builder()
            .nickname(nickname)
            .tier(UserTierType.B3)
            .build();
    }

    @Nested
    @DisplayName("사용자 목록 조회")
    class GetUsers {

        @Test
        @DisplayName("검색어 없이 전체 사용자 목록 조회")
        void getUsers_Success_AllUsers() {
            // given
            List<UserAccountEntity> users = List.of(
                createMockUserEntity(1L, "alice"),
                createMockUserEntity(2L, "bob")
            );
            List<UserProfileResponseDto> expectedProfiles = List.of(
                createMockUserProfileResponseDto("alice"),
                createMockUserProfileResponseDto("bob")
            );

            given(userLookupService.findAllUsers()).willReturn(users);
            given(userDataAggregationService.buildProfilesBatch(users)).willReturn(expectedProfiles);

            // when
            List<UserProfileResponseDto> result = userProfileService.getUsers(null);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).nickname()).isEqualTo("alice");
            assertThat(result.get(1).nickname()).isEqualTo("bob");

            then(userLookupService).should().findAllUsers();
            then(userLookupService).should(never()).findUsersByNicknameContaining(anyString());
        }

        @Test
        @DisplayName("검색어로 사용자 검색")
        void getUsers_Success_WithSearch() {
            // given
            String searchTerm = "alice";
            List<UserAccountEntity> users = List.of(
                createMockUserEntity(1L, "alice")
            );
            UserProfileResponseDto expectedProfile = createMockUserProfileResponseDto("alice");

            given(userLookupService.findUsersByNicknameContaining(searchTerm)).willReturn(users);
            given(userDataAggregationService.buildProfilesBatch(users)).willReturn(List.of(expectedProfile));

            // when
            List<UserProfileResponseDto> result = userProfileService.getUsers(searchTerm);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("alice");

            then(userLookupService).should().findUsersByNicknameContaining(searchTerm);
            then(userLookupService).should(never()).findAllUsers();
        }

        @Test
        @DisplayName("공백 검색어는 전체 조회로 처리")
        void getUsers_Success_BlankSearch() {
            // given
            List<UserAccountEntity> users = List.of(createMockUserEntity(1L, "alice"));
            UserProfileResponseDto expectedProfile = createMockUserProfileResponseDto("alice");

            given(userLookupService.findAllUsers()).willReturn(users);
            given(userDataAggregationService.buildProfilesBatch(users)).willReturn(List.of(expectedProfile));

            // when
            List<UserProfileResponseDto> result = userProfileService.getUsers("   ");

            // then
            assertThat(result).hasSize(1);
            then(userLookupService).should().findAllUsers();
        }
    }

    @Nested
    @DisplayName("사용자 개별 조회")
    class GetUser {

        @Test
        @DisplayName("ID로 사용자 조회 성공")
        void getUserById_Success() {
            // given
            Long userId = 1L;
            UserAccountEntity user = createMockUserAccountEntity(userId, "alice");
            UserProfileResponseDto expectedProfile = createMockUserProfileResponseDto("alice");

            given(userLookupService.findUserById(userId)).willReturn(user);
            given(userDataAggregationService.buildProfile(user)).willReturn(expectedProfile);

            // when
            UserProfileResponseDto result = userProfileService.getUserById(userId);

            // then
            assertThat(result.nickname()).isEqualTo("alice");
            then(userLookupService).should().findUserById(userId);
        }

        @Test
        @DisplayName("닉네임으로 사용자 조회 성공")
        void getUserByNickname_Success() {
            // given
            String nickname = "alice";
            UserAccountEntity user = createMockUserAccountEntity(1L, nickname);
            UserProfileResponseDto expectedProfile = createMockUserProfileResponseDto(nickname);

            given(userLookupService.findUserByNickname(nickname)).willReturn(user);
            given(userDataAggregationService.buildProfile(user)).willReturn(expectedProfile);

            // when
            UserProfileResponseDto result = userProfileService.getUserByNickname(nickname);

            // then
            assertThat(result.nickname()).isEqualTo("alice");
            then(userLookupService).should().findUserByNickname(nickname);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void getUserById_NotFound() {
            // given
            Long userId = 999L;
            given(userLookupService.findUserById(userId)).willThrow(
                new UserNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> userProfileService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("프로필 정보 수정")
    class ModifyProfile {

        @Test
        @DisplayName("프로필 정보 수정 성공")
        void modifyUserProfileInfo_Success() {
            // given
            Long userId = 1L;
            String currentNickname = "alice";
            String newNickname = "alice_new";
            String newStatusMessage = "새로운 상태메시지";

            UserProfileInfoModifyRequestDto requestDto = new UserProfileInfoModifyRequestDto(
                newNickname, newStatusMessage
            );

            UserAccountEntity user = createMockUserAccountEntity(userId, currentNickname);
            UserProfileResponseDto expectedProfile = createMockUserProfileResponseDto(newNickname);

            given(userLookupService.findUserById(userId)).willReturn(user);
            given(userAccountRepository.existsByNicknameIgnoringRole(newNickname)).willReturn(false);
            given(userDataAggregationService.buildProfile(user)).willReturn(expectedProfile);

            // when
            UserProfileResponseDto result = userProfileService.modifyUserProfileInfo(
                userId, currentNickname, requestDto
            );

            // then
            assertThat(result.nickname()).isEqualTo(newNickname);
            then(userAccountRepository).should().existsByNicknameIgnoringRole(newNickname);
        }

        @Test
        @DisplayName("현재 닉네임과 요청 닉네임이 다를 때 예외 발생")
        void modifyUserProfileInfo_NicknameMismatch() {
            // given
            Long userId = 1L;
            String currentNickname = "alice";
            String wrongNickname = "bob";
            UserAccountEntity user = createMockUserAccountEntity(userId, currentNickname);
            UserProfileInfoModifyRequestDto requestDto = new UserProfileInfoModifyRequestDto(
                "new_alice", "새 상태메시지"
            );

            given(userLookupService.findUserById(userId)).willReturn(user);

            // when & then
            assertThatThrownBy(() -> userProfileService.modifyUserProfileInfo(
                userId, wrongNickname, requestDto
            )).isInstanceOf(NicknameMismatchException.class);
        }

        @Test
        @DisplayName("중복된 닉네임으로 수정 시 예외 발생")
        void modifyUserProfileInfo_DuplicateNickname() {
            // given
            Long userId = 1L;
            String currentNickname = "alice";
            String duplicateNickname = "bob";
            UserAccountEntity user = createMockUserAccountEntity(userId, currentNickname);
            UserProfileInfoModifyRequestDto requestDto = new UserProfileInfoModifyRequestDto(
                duplicateNickname, "새 상태메시지"
            );

            given(userLookupService.findUserById(userId)).willReturn(user);
            given(userAccountRepository.existsByNicknameIgnoringRole(duplicateNickname)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userProfileService.modifyUserProfileInfo(
                userId, currentNickname, requestDto
            )).isInstanceOf(DuplicateNicknameException.class);
        }
    }

    @Nested
    @DisplayName("프로필 이미지 업데이트")
    class UpdateProfileImage {

        @Test
        @DisplayName("프로필 이미지 업로드 성공")
        void updateUserProfileImage_Success() {
            // given
            Long userId = 1L;
            String nickname = "alice";
            MockMultipartFile profileImage = new MockMultipartFile(
                "image", "profile.jpg", "image/jpeg", "image content".getBytes()
            );
            String uploadedUrl = "https://s3.amazonaws.com/profile.jpg";

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            UserProfileResponseDto expectedProfile = createMockUserProfileResponseDto(nickname);

            given(userLookupService.findUserById(userId)).willReturn(user);
            given(s3Service.uploadProfileImage(userId, profileImage)).willReturn(uploadedUrl);
            given(userDataAggregationService.buildProfile(user)).willReturn(expectedProfile);

            // when
            UserProfileResponseDto result = userProfileService.updateUserProfileImage(
                userId, nickname, profileImage
            );

            // then
            assertThat(result.nickname()).isEqualTo(nickname);
            then(s3Service).should().uploadProfileImage(userId, profileImage);
        }

        @Test
        @DisplayName("빈 이미지 파일로 업로드 시 기본 이미지로 설정")
        void updateUserProfileImage_EmptyFile() {
            // given
            Long userId = 1L;
            String nickname = "alice";
            MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "", "image/jpeg", new byte[0]
            );

            UserAccountEntity user = createMockUserAccountEntity(userId, nickname);
            UserProfileResponseDto expectedProfile = createMockUserProfileResponseDto(nickname);

            given(userLookupService.findUserById(userId)).willReturn(user);
            given(userDataAggregationService.buildProfile(user)).willReturn(expectedProfile);

            // when
            UserProfileResponseDto result = userProfileService.updateUserProfileImage(
                userId, nickname, emptyFile
            );

            // then
            assertThat(result.nickname()).isEqualTo(nickname);
            then(s3Service).should(never()).uploadProfileImage(anyLong(), any());
        }

        @Test
        @DisplayName("닉네임 불일치 시 예외 발생")
        void updateUserProfileImage_NicknameMismatch() {
            // given
            Long userId = 1L;
            String userNickname = "alice";
            String wrongNickname = "bob";
            MockMultipartFile profileImage = new MockMultipartFile(
                "image", "profile.jpg", "image/jpeg", "image content".getBytes()
            );

            UserAccountEntity user = createMockUserAccountEntity(userId, userNickname);
            given(userLookupService.findUserById(userId)).willReturn(user);

            // when & then
            assertThatThrownBy(() -> userProfileService.updateUserProfileImage(
                userId, wrongNickname, profileImage
            )).isInstanceOf(NicknameMismatchException.class);
        }
    }
}