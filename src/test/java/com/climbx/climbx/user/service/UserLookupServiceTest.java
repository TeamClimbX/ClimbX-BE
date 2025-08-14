package com.climbx.climbx.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserLookupServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserLookupService userLookupService;

    private UserAccountEntity createMockUserAccountEntity(Long userId, String nickname) {
        return UserAccountEntity.builder()
            .userId(userId)
            .nickname(nickname)
            .role(RoleType.USER)
            .build();
    }

    @Nested
    @DisplayName("ID로 사용자 조회")
    class FindUserById {

        @Test
        @DisplayName("사용자 ID로 조회 성공")
        void findUserById_Success() {
            // given
            Long userId = 1L;
            UserAccountEntity user = createMockUserAccountEntity(userId, "alice");

            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.of(user));

            // when
            UserAccountEntity result = userLookupService.findUserById(userId);

            // then
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.nickname()).isEqualTo("alice");
            then(userAccountRepository).should().findByUserId(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 시 예외 발생")
        void findUserById_NotFound() {
            // given
            Long userId = 999L;
            given(userAccountRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userLookupService.findUserById(userId))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("닉네임으로 사용자 조회")
    class FindUserByNickname {

        @Test
        @DisplayName("닉네임으로 조회 성공")
        void findUserByNickname_Success() {
            // given
            String nickname = "alice";
            UserAccountEntity user = createMockUserAccountEntity(1L, nickname);

            given(userAccountRepository.findByNickname(nickname)).willReturn(Optional.of(user));

            // when
            UserAccountEntity result = userLookupService.findUserByNickname(nickname);

            // then
            assertThat(result.nickname()).isEqualTo(nickname);
            assertThat(result.userId()).isEqualTo(1L);
            then(userAccountRepository).should().findByNickname(nickname);
        }

        @Test
        @DisplayName("존재하지 않는 닉네임으로 조회 시 예외 발생")
        void findUserByNickname_NotFound() {
            // given
            String nickname = "nonexistent";
            given(userAccountRepository.findByNickname(nickname)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userLookupService.findUserByNickname(nickname))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("전체 사용자 조회")
    class FindAllUsers {

        @Test
        @DisplayName("전체 사용자 목록 조회 성공")
        void findAllUsers_Success() {
            // given
            List<UserAccountEntity> users = List.of(
                createMockUserAccountEntity(1L, "alice"),
                createMockUserAccountEntity(2L, "bob")
            );

            given(userAccountRepository.findAll()).willReturn(users);

            // when
            List<UserAccountEntity> result = userLookupService.findAllUsers();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).nickname()).isEqualTo("alice");
            assertThat(result.get(1).nickname()).isEqualTo("bob");
            then(userAccountRepository).should().findAll();
        }

        @Test
        @DisplayName("사용자가 없는 경우 빈 목록 반환")
        void findAllUsers_EmptyList() {
            // given
            given(userAccountRepository.findAll()).willReturn(List.of());

            // when
            List<UserAccountEntity> result = userLookupService.findAllUsers();

            // then
            assertThat(result).isEmpty();
            then(userAccountRepository).should().findAll();
        }
    }

    @Nested
    @DisplayName("닉네임 부분 매칭 사용자 조회")
    class FindUsersByNicknameContaining {

        @Test
        @DisplayName("닉네임 부분 매칭 조회 성공")
        void findUsersByNicknameContaining_Success() {
            // given
            String searchTerm = "ali";
            List<UserAccountEntity> users = List.of(
                createMockUserAccountEntity(1L, "alice")
            );

            given(userAccountRepository.findByNicknameContaining(searchTerm))
                .willReturn(users);

            // when
            List<UserAccountEntity> result = userLookupService.findUsersByNicknameContaining(
                searchTerm);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("alice");
            then(userAccountRepository).should().findByNicknameContaining(searchTerm);
        }

        @Test
        @DisplayName("매칭되는 사용자가 없는 경우")
        void findUsersByNicknameContaining_NoMatches() {
            // given
            String searchTerm = "xyz";
            given(userAccountRepository.findByNicknameContaining(searchTerm))
                .willReturn(List.of());

            // when
            List<UserAccountEntity> result = userLookupService.findUsersByNicknameContaining(
                searchTerm);

            // then
            assertThat(result).isEmpty();
            then(userAccountRepository).should().findByNicknameContaining(searchTerm);
        }

        @Test
        @DisplayName("기본 검색 동작 확인")
        void findUsersByNicknameContaining_BasicSearch() {
            // given
            String searchTerm = "test";
            List<UserAccountEntity> users = List.of(
                createMockUserAccountEntity(1L, "testuser")
            );

            given(userAccountRepository.findByNicknameContaining(searchTerm))
                .willReturn(users);

            // when
            List<UserAccountEntity> result = userLookupService.findUsersByNicknameContaining(
                searchTerm);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("testuser");
        }
    }
}