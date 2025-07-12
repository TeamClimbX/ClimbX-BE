package com.climbx.climbx.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.climbx.climbx.common.comcode.ComcodeService;
import com.climbx.climbx.fixture.GymFixture;
import com.climbx.climbx.fixture.ProblemFixture;
import com.climbx.climbx.fixture.UserFixture;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.problem.dto.ProblemDetailsResponseDto;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.repository.ProblemRepository;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserStatRepository userStatRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRankingHistoryRepository userRankingHistoryRepository;

    @Mock
    private ComcodeService comcodeService;

    @InjectMocks
    private UserService userService;

    private void setupUserRoleComcode() {
        given(comcodeService.getCodeValue("USER"))
            .willReturn("USER");
    }

    private void setupRatingComcode() {
        given(comcodeService.getCodeValue("RATING"))
            .willReturn("RATING");
    }

    private void setupRankingComcode() {
        given(comcodeService.getCodeValue("RANKING"))
            .willReturn("RANKING");
    }

    private void setupSolvedCountComcode() {
        given(comcodeService.getCodeValue("SOLVED_COUNT"))
            .willReturn("SOLVED_COUNT");
    }

    private void setupAcceptedComcode() {
        given(comcodeService.getCodeValue("ACCEPTED"))
            .willReturn("ACCEPTED");
    }

    @Nested
    @DisplayName("사용자 목록 조회 및 검색")
    class GetUsers {

        @Test
        @DisplayName("전체 사용자 목록을 정상 조회")
        void getUsers_Success_AllUsers() {
            // given
            setupUserRoleComcode();
            String search = null;

            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "alice");
            UserAccountEntity user2 = UserFixture.createUserAccountEntity(2L, "bob");
            UserAccountEntity user3 = UserFixture.createUserAccountEntity(3L, "charlie");
            List<UserAccountEntity> userAccounts = List.of(user1, user2, user3);

            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L, 1200L);
            UserStatEntity userStat2 = UserFixture.createUserStatEntity(2L, 1300L);
            UserStatEntity userStat3 = UserFixture.createUserStatEntity(3L, 1400L);

            given(userAccountRepository.findByRole("USER"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.of(userStat1));
            given(userStatRepository.findByUserId(2L))
                .willReturn(Optional.of(userStat2));
            given(userStatRepository.findByUserId(3L))
                .willReturn(Optional.of(userStat3));
            given(userStatRepository.findRatingRank(1200L))
                .willReturn(30L);
            given(userStatRepository.findRatingRank(1300L))
                .willReturn(20L);
            given(userStatRepository.findRatingRank(1400L))
                .willReturn(10L);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).nickname()).isEqualTo("alice");
            assertThat(result.get(1).nickname()).isEqualTo("bob");
            assertThat(result.get(2).nickname()).isEqualTo("charlie");

            then(userAccountRepository).should().findByRole("USER");
            then(userAccountRepository).should(never())
                .findByRoleAndNicknameContaining(any(), any());
        }

        @Test
        @DisplayName("빈 문자열로 검색 시 전체 사용자 목록 조회")
        void getUsers_Success_EmptySearch() {
            // given
            setupUserRoleComcode();
            String search = "";

            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "test1");
            UserAccountEntity user2 = UserFixture.createUserAccountEntity(2L, "test2");
            List<UserAccountEntity> userAccounts = List.of(user1, user2);

            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L);
            UserStatEntity userStat2 = UserFixture.createUserStatEntity(2L);

            given(userAccountRepository.findByRole("USER"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.of(userStat1));
            given(userStatRepository.findByUserId(2L))
                .willReturn(Optional.of(userStat2));
            given(userStatRepository.findRatingRank(UserFixture.DEFAULT_RATING))
                .willReturn(UserFixture.DEFAULT_RANKING);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(2);
            then(userAccountRepository).should().findByRole("USER");
            then(userAccountRepository).should(never())
                .findByRoleAndNicknameContaining(any(), any());
        }

        @Test
        @DisplayName("공백만 있는 검색어로 검색 시 전체 사용자 목록 조회")
        void getUsers_Success_WhitespaceOnlySearch() {
            // given
            setupUserRoleComcode();
            String search = "   ";

            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "user1");
            List<UserAccountEntity> userAccounts = List.of(user1);

            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L);

            given(userAccountRepository.findByRole("USER"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.of(userStat1));
            given(userStatRepository.findRatingRank(UserFixture.DEFAULT_RATING))
                .willReturn(UserFixture.DEFAULT_RANKING);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(1);
            then(userAccountRepository).should().findByRole("USER");
            then(userAccountRepository).should(never())
                .findByRoleAndNicknameContaining(any(), any());
        }

        @Test
        @DisplayName("닉네임 검색으로 특정 사용자들 조회")
        void getUsers_Success_WithSearch() {
            // given
            setupUserRoleComcode();
            String search = "test";

            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "testuser1");
            UserAccountEntity user2 = UserFixture.createUserAccountEntity(2L, "testuser2");
            List<UserAccountEntity> userAccounts = List.of(user1, user2);

            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L, 1100L);
            UserStatEntity userStat2 = UserFixture.createUserStatEntity(2L, 1600L);

            given(userAccountRepository.findByRoleAndNicknameContaining("USER", "test"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.of(userStat1));
            given(userStatRepository.findByUserId(2L))
                .willReturn(Optional.of(userStat2));
            given(userStatRepository.findRatingRank(1100L))
                .willReturn(40L);
            given(userStatRepository.findRatingRank(1600L))
                .willReturn(5L);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).nickname()).isEqualTo("testuser1");
            assertThat(result.get(1).nickname()).isEqualTo("testuser2");

            then(userAccountRepository).should()
                .findByRoleAndNicknameContaining("USER", "test");
            then(userAccountRepository).should(never()).findByRole(any());
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void getUsers_Success_NoResults() {
            // given
            setupUserRoleComcode();
            String search = "nonexistent";
            List<UserAccountEntity> emptyUserAccounts = List.of();

            given(
                userAccountRepository.findByRoleAndNicknameContaining("USER", "nonexistent"))
                .willReturn(emptyUserAccounts);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).isEmpty();

            then(userAccountRepository).should()
                .findByRoleAndNicknameContaining("USER", "nonexistent");
            then(userStatRepository).should(never()).findByUserId(any());
        }

        @Test
        @DisplayName("검색어 앞뒤 공백 제거 후 검색")
        void getUsers_Success_TrimmedSearch() {
            // given
            setupUserRoleComcode();
            String search = "  alice  ";

            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "alice123");
            List<UserAccountEntity> userAccounts = List.of(user1);

            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L);

            given(userAccountRepository.findByRoleAndNicknameContaining("USER", "alice"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.of(userStat1));
            given(userStatRepository.findRatingRank(UserFixture.DEFAULT_RATING))
                .willReturn(UserFixture.DEFAULT_RANKING);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("alice123");

            then(userAccountRepository).should()
                .findByRoleAndNicknameContaining("USER", "alice");
        }

        @Test
        @DisplayName("사용자는 있지만 통계 정보가 없는 경우")
        void getUsers_UserStatNotFound() {
            // given
            setupUserRoleComcode();
            String search = null;

            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "user1");
            List<UserAccountEntity> userAccounts = List.of(user1);

            given(userAccountRepository.findByRole("USER"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUsers(search))
                .isInstanceOf(UserStatNotFoundException.class);
        }

        @Test
        @DisplayName("다양한 레이팅을 가진 사용자들 조회")
        void getUsers_Success_DifferentRatings() {
            // given
            setupUserRoleComcode();
            String search = "pro";

            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "pro_player1");
            UserAccountEntity user2 = UserFixture.createUserAccountEntity(2L, "pro_player2");
            UserAccountEntity user3 = UserFixture.createUserAccountEntity(3L, "pro_player3");
            List<UserAccountEntity> userAccounts = List.of(user1, user2, user3);

            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L, 2000L, 10L, 20L, 100L,
                5L);
            UserStatEntity userStat2 = UserFixture.createUserStatEntity(2L, 1800L, 8L, 15L, 80L,
                3L);
            UserStatEntity userStat3 = UserFixture.createUserStatEntity(3L, 2200L, 15L, 25L, 120L,
                7L);

            given(userAccountRepository.findByRoleAndNicknameContaining("USER", "pro"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.of(userStat1));
            given(userStatRepository.findByUserId(2L))
                .willReturn(Optional.of(userStat2));
            given(userStatRepository.findByUserId(3L))
                .willReturn(Optional.of(userStat3));
            given(userStatRepository.findRatingRank(2000L))
                .willReturn(3L);
            given(userStatRepository.findRatingRank(1800L))
                .willReturn(8L);
            given(userStatRepository.findRatingRank(2200L))
                .willReturn(1L);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(3);

            UserProfileResponseDto firstUser = result.get(0);
            assertThat(firstUser.nickname()).isEqualTo("pro_player1");
            assertThat(firstUser.rating()).isEqualTo(2000L);
            assertThat(firstUser.ranking()).isEqualTo(3L);
            assertThat(firstUser.solvedProblemsCount()).isEqualTo(100L);

            UserProfileResponseDto secondUser = result.get(1);
            assertThat(secondUser.nickname()).isEqualTo("pro_player2");
            assertThat(secondUser.rating()).isEqualTo(1800L);
            assertThat(secondUser.ranking()).isEqualTo(8L);

            UserProfileResponseDto thirdUser = result.get(2);
            assertThat(thirdUser.nickname()).isEqualTo("pro_player3");
            assertThat(thirdUser.rating()).isEqualTo(2200L);
            assertThat(thirdUser.ranking()).isEqualTo(1L);
        }

        @Test
        @DisplayName("ADMIN 역할 사용자는 조회되지 않음")
        void getUsers_AdminNotIncluded() {
            // given
            setupUserRoleComcode();
            String search = null;

            UserAccountEntity adminUser = UserFixture.createAdminUserAccountEntity(1L, "admin");
            UserAccountEntity normalUser = UserFixture.createUserAccountEntity(2L, "user");
            List<UserAccountEntity> userAccounts = List.of(normalUser); // admin은 포함되지 않음

            UserStatEntity userStat = UserFixture.createUserStatEntity(2L);

            given(userAccountRepository.findByRole("USER"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(2L))
                .willReturn(Optional.of(userStat));
            given(userStatRepository.findRatingRank(UserFixture.DEFAULT_RATING))
                .willReturn(UserFixture.DEFAULT_RANKING);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("user");

            then(userAccountRepository).should().findByRole("USER");
            then(userAccountRepository).should(never()).findByRole("ADMIN");
        }

        @Test
        @DisplayName("ADMIN 역할 사용자는 검색에서도 제외됨")
        void getUsers_AdminNotIncludedInSearch() {
            // given
            setupUserRoleComcode();
            String search = "admin";

            UserAccountEntity normalUser = UserFixture.createUserAccountEntity(1L, "admin_user");
            List<UserAccountEntity> userAccounts = List.of(normalUser); // admin 역할이 아닌 사용자만 포함

            UserStatEntity userStat = UserFixture.createUserStatEntity(1L);

            given(userAccountRepository.findByRoleAndNicknameContaining("USER", "admin"))
                .willReturn(userAccounts);
            given(userStatRepository.findByUserId(1L))
                .willReturn(Optional.of(userStat));
            given(userStatRepository.findRatingRank(UserFixture.DEFAULT_RATING))
                .willReturn(UserFixture.DEFAULT_RANKING);

            // when
            List<UserProfileResponseDto> result = userService.getUsers(search);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("admin_user");

            then(userAccountRepository).should()
                .findByRoleAndNicknameContaining("USER", "admin");
            then(userAccountRepository).should(never()).findByRole(any());
        }
    }

    @Nested
    @DisplayName("사용자 ID로 프로필 조회")
    class GetUserById {

        @Test
        @DisplayName("사용자 ID로 프로필을 정상 조회")
        void getUserById_Success() {
            // given
            Long userId = 1L;
            Long ratingRank = 10L;

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(userId);
            UserStatEntity userStatEntity = UserFixture.createUserStatEntity(userId);

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.of(userAccountEntity));
            given(userStatRepository.findByUserId(userId))
                .willReturn(Optional.of(userStatEntity));
            given(userStatRepository.findRatingRank(UserFixture.DEFAULT_RATING))
                .willReturn(ratingRank);

            // when
            UserProfileResponseDto result = userService.getUserById(userId);

            // then
            UserProfileResponseDto expected = UserFixture.createUserProfileResponseDto(
                UserFixture.DEFAULT_NICKNAME, ratingRank);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회")
        void getUserById_UserNotFound() {
            // given
            Long userId = 999L;
            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("사용자는 존재하지만 통계 정보가 없음")
        void getUserById_UserStatNotFound() {
            // given
            Long userId = 1L;
            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(userId);

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.of(userAccountEntity));
            given(userStatRepository.findByUserId(userId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserStatNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("사용자 닉네임으로 프로필 조회")
    class GetUserByNickname {

        @Test
        @DisplayName("닉네임으로 사용자 프로필을 정상 조회")
        void getUserByNickname_Success() {
            // given
            String nickname = "testUser";
            Long userId = 1L;
            Long ratingRank = 10L;

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(userId,
                nickname);
            UserStatEntity userStatEntity = UserFixture.createUserStatEntity(userId);

            given(userAccountRepository.findByNickname(nickname))
                .willReturn(Optional.of(userAccountEntity));
            given(userStatRepository.findByUserId(userId))
                .willReturn(Optional.of(userStatEntity));
            given(userStatRepository.findRatingRank(UserFixture.DEFAULT_RATING))
                .willReturn(ratingRank);

            // when
            UserProfileResponseDto result = userService.getUserByNickname(nickname);

            // then
            UserProfileResponseDto expected = UserFixture.createUserProfileResponseDto(nickname,
                ratingRank);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 닉네임으로 조회")
        void getUserByNickname_UserNotFound() {
            // given
            String nickname = "nonexistentUser";
            given(userAccountRepository.findByNickname(nickname))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserByNickname(nickname))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("사용자는 존재하지만 통계 정보가 없음")
        void getUserByNickname_UserStatNotFound() {
            // given
            String nickname = "testUser";
            Long userId = 1L;

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(userId,
                nickname);

            given(userAccountRepository.findByNickname(nickname))
                .willReturn(Optional.of(userAccountEntity));
            given(userStatRepository.findByUserId(userId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserByNickname(nickname))
                .isInstanceOf(UserStatNotFoundException.class);
        }

        @Test
        @DisplayName("닉네임이 null인 사용자 조회")
        void getUserByNickname_NullNickname() {
            // given
            given(userAccountRepository.findByNickname(null))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserByNickname(null))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("빈 문자열 닉네임으로 사용자 조회")
        void getUserByNickname_EmptyNickname() {
            // given
            String emptyNickname = "";
            given(userAccountRepository.findByNickname(emptyNickname))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserByNickname(emptyNickname))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("사용자 프로필 수정")
    class ModifyUserProfile {

        @Test
        @DisplayName("사용자 프로필 정상 수정")
        void modifyUserProfile_Success() {
            // given
            Long userId = 1L;
            String currentNickname = "oldNickname";
            String newNickname = "newNickname";
            String newStatusMessage = "New status";
            String newProfileImageUrl = "new.jpg";
            Long rating = 1200L;
            Long ratingRank = 20L;

            UserProfileModifyRequestDto requestDto = new UserProfileModifyRequestDto(
                newNickname, newStatusMessage, newProfileImageUrl);

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(
                userId, currentNickname, "Old status", "old.jpg");
            UserStatEntity userStatEntity = UserFixture.createUserStatEntity(
                userId, rating, 3L, 10L, 15L, 2L);

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.of(userAccountEntity));
            given(userAccountRepository.existsByNickname(newNickname))
                .willReturn(false);
            given(userStatRepository.findByUserId(userId))
                .willReturn(Optional.of(userStatEntity));
            given(userStatRepository.findRatingRank(rating))
                .willReturn(ratingRank);

            // when
            UserProfileResponseDto result = userService.modifyUserProfile(userId, currentNickname,
                requestDto);

            // then
            UserProfileResponseDto expected = UserFixture.createUserProfileResponseDto(
                newNickname, newStatusMessage, newProfileImageUrl, ratingRank, rating, 3L, 10L, 15L,
                2L);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 수정 시도")
        void modifyUserProfile_UserNotFound() {
            // given
            Long userId = 999L;
            String currentNickname = "oldNickname";
            UserProfileModifyRequestDto requestDto = new UserProfileModifyRequestDto(
                "newNickname", "New status", "new.jpg");

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> userService.modifyUserProfile(userId, currentNickname, requestDto))
                .isInstanceOf(UserNotFoundException.class);
            then(userAccountRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("현재 닉네임과 요청 닉네임이 일치하지 않음")
        void modifyUserProfile_NicknameMismatch() {
            // given
            Long userId = 1L;
            String currentNickname = "oldNickname";
            String actualNickname = "actualNickname";

            UserProfileModifyRequestDto requestDto = new UserProfileModifyRequestDto(
                "newNickname", "New status", "new.jpg");

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(userId,
                actualNickname);

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.of(userAccountEntity));

            // when & then
            assertThatThrownBy(
                () -> userService.modifyUserProfile(userId, currentNickname, requestDto))
                .isInstanceOf(NicknameMismatchException.class);

            then(userAccountRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("중복된 닉네임으로 수정 시도")
        void modifyUserProfile_DuplicateNickname() {
            // given
            Long userId = 1L;
            String currentNickname = "oldNickname";
            String duplicateNickname = "existingNickname";

            UserProfileModifyRequestDto requestDto = new UserProfileModifyRequestDto(
                duplicateNickname, "New status", "new.jpg");

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(userId,
                currentNickname);

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.of(userAccountEntity));
            given(userAccountRepository.existsByNickname(duplicateNickname))
                .willReturn(true);

            // when & then
            assertThatThrownBy(
                () -> userService.modifyUserProfile(userId, currentNickname, requestDto))
                .isInstanceOf(DuplicateNicknameException.class);
            then(userAccountRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("같은 닉네임으로 수정하는 경우 중복 체크 안함")
        void modifyUserProfile_SameNickname_NoDuplicateCheck() {
            // given
            Long userId = 1L;
            String currentNickname = "sameNickname";

            UserProfileModifyRequestDto requestDto = new UserProfileModifyRequestDto(
                currentNickname, "New status", "new.jpg");

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(
                userId, currentNickname, "Old status", "old.jpg");
            UserStatEntity userStatEntity = UserFixture.createUserStatEntity(userId, 1000L);

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.of(userAccountEntity));
            given(userStatRepository.findByUserId(userId))
                .willReturn(Optional.of(userStatEntity));
            given(userStatRepository.findRatingRank(1000L))
                .willReturn(50L);

            // when
            UserProfileResponseDto result = userService.modifyUserProfile(userId, currentNickname,
                requestDto);

            // then
            assertThat(result).isNotNull();
            then(userAccountRepository).should(never()).existsByNickname(any());
        }

        @Test
        @DisplayName("프로필 수정 후 사용자 통계 조회 실패")
        void modifyUserProfile_UserStatNotFoundAfterUpdate() {
            // given
            Long userId = 1L;
            String currentNickname = "oldNickname";
            String newNickname = "newNickname";

            UserProfileModifyRequestDto requestDto = new UserProfileModifyRequestDto(
                newNickname, "New status", "new.jpg");

            UserAccountEntity userAccountEntity = UserFixture.createUserAccountEntity(userId,
                currentNickname);

            given(userAccountRepository.findByUserId(userId))
                .willReturn(Optional.of(userAccountEntity));
            given(userAccountRepository.existsByNickname(newNickname))
                .willReturn(false);
            given(userStatRepository.findByUserId(userId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> userService.modifyUserProfile(userId, currentNickname, requestDto))
                .isInstanceOf(UserStatNotFoundException.class);
        }

        @Nested
        @DisplayName("사용자 상위 문제 조회")
        class GetUserTopProblems {

            @Test
            @DisplayName("사용자의 상위 문제를 정상 조회")
            void getUserTopProblems_Success() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                Integer limit = 5;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                GymEntity gym1 = GymFixture.createGymEntity(1L, "테스트 체육관1", 37.5665, 126.9780);
                GymEntity gym2 = GymFixture.createGymEntity(2L, "테스트 체육관2", 37.5665, 126.9780);
                GymEntity gym3 = GymFixture.createGymEntity(3L, "테스트 체육관3", 37.5665, 126.9780);

                ProblemEntity problem1 = ProblemFixture.createProblemEntity(1L, gym1, "고급", "빨강",
                    1800L);
                ProblemEntity problem2 = ProblemFixture.createProblemEntity(2L, gym2, "중급", "파랑",
                    1500L);
                ProblemEntity problem3 = ProblemFixture.createProblemEntity(3L, gym3, "초급", "노랑",
                    1200L);

                List<ProblemEntity> problemEntities = List.of(problem1, problem2, problem3);

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(
                    submissionRepository.getUserSubmissionProblems(eq(userId), eq("ACCEPTED"),
                        any(Pageable.class)))
                    .willReturn(problemEntities);

                // when
                List<ProblemDetailsResponseDto> result = userService.getUserTopProblems(nickname,
                    limit);

                // then
                List<ProblemDetailsResponseDto> expected = List.of(
                    ProblemFixture.createProblemResponseDto(1L, 1L, "테스트 체육관1", "고급", "빨강", 1800L),
                    ProblemFixture.createProblemResponseDto(2L, 2L, "테스트 체육관2", "중급", "파랑", 1500L),
                    ProblemFixture.createProblemResponseDto(3L, 3L, "테스트 체육관3", "초급", "노랑", 1200L)
                );
                assertThat(result).isEqualTo(expected);

                then(submissionRepository).should()
                    .getUserSubmissionProblems(eq(userId), eq("ACCEPTED"), any(Pageable.class));
            }

            @Test
            @DisplayName("존재하지 않는 닉네임으로 상위 문제 조회")
            void getUserTopProblems_UserNotFound() {
                // given
                String nickname = "nonexistentUser";
                Integer limit = 5;

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> userService.getUserTopProblems(nickname, limit))
                    .isInstanceOf(UserNotFoundException.class);
                then(submissionRepository).should(never())
                    .getUserSubmissionProblems(any(), any(), any());
            }

            @Test
            @DisplayName("사용자에게 문제 제출 기록이 없는 경우")
            void getUserTopProblems_NoSubmissions() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                Integer limit = 5;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);
                List<ProblemEntity> emptyProblems = List.of();

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(
                    submissionRepository.getUserSubmissionProblems(eq(userId), eq("ACCEPTED"),
                        any(Pageable.class)))
                    .willReturn(emptyProblems);

                // when
                List<ProblemDetailsResponseDto> result = userService.getUserTopProblems(nickname,
                    limit);

                // then
                assertThat(result).isEmpty();
                then(submissionRepository).should()
                    .getUserSubmissionProblems(eq(userId), eq("ACCEPTED"), any(Pageable.class));
            }

            @Test
            @DisplayName("limit이 0인 경우 예외 발생")
            void getUserTopProblems_ZeroLimit() {
                // given
                String nickname = "testUser";
                Long userId = 1L;
                Integer limit = 0;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));

                // when & then
                assertThatThrownBy(() -> userService.getUserTopProblems(nickname, limit))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("요청한 limit보다 적은 문제가 있는 경우")
            void getUserTopProblems_LessProblemsThanlimit() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                Integer limit = 10;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                GymEntity gym1 = GymFixture.createGymEntity(1L, "테스트 체육관1", 37.5665, 126.9780);
                GymEntity gym2 = GymFixture.createGymEntity(2L, "테스트 체육관2", 37.5665, 126.9780);

                ProblemEntity problem1 = ProblemFixture.createProblemEntity(1L, gym1, "고급", "빨강",
                    1600L);
                ProblemEntity problem2 = ProblemFixture.createProblemEntity(2L, gym2, "중급", "파랑",
                    1400L);

                List<ProblemEntity> problemEntities = List.of(problem1, problem2);

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(
                    submissionRepository.getUserSubmissionProblems(eq(userId), eq("ACCEPTED"),
                        any(Pageable.class)))
                    .willReturn(problemEntities);

                // when
                List<ProblemDetailsResponseDto> result = userService.getUserTopProblems(nickname,
                    limit);

                // then
                List<ProblemDetailsResponseDto> expected = List.of(
                    ProblemFixture.createProblemResponseDto(1L, 1L, "테스트 체육관1", "고급", "빨강", 1600L),
                    ProblemFixture.createProblemResponseDto(2L, 2L, "테스트 체육관2", "중급", "파랑", 1400L)
                );
                assertThat(result).isEqualTo(expected);
            }
        }

        @Nested
        @DisplayName("사용자 스트릭 조회")
        class GetUserStreak {

            @Test
            @DisplayName("사용자의 일별 해결 문제 수를 정상 조회")
            void getUserStreak_Success() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 3);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 3L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 5L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 3), 2L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname, from,
                    to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 3L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 5L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 3), 2L)
                );
                assertThat(result).isEqualTo(expected);

                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", from, to);
            }

            @Test
            @DisplayName("존재하지 않는 닉네임으로 스트릭 조회")
            void getUserStreak_UserNotFound() {
                // given
                String nickname = "nonexistentUser";
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 31);

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> userService.getUserStreak(nickname, from, to))
                    .isInstanceOf(UserNotFoundException.class);

                then(submissionRepository).should(never())
                    .getUserDateSolvedCount(any(), any(), any(), any());
            }

            @Test
            @DisplayName("해당 기간에 해결한 문제가 없는 경우")
            void getUserStreak_NoSolvedProblems() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 31);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);
                List<DailyHistoryResponseDto> emptyResults = List.of();

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", from, to))
                    .willReturn(emptyResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname, from,
                    to);

                // then
                assertThat(result).isEmpty();
                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", from, to);
            }

            @Test
            @DisplayName("하루만 조회하는 경우")
            void getUserStreak_SingleDay() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate singleDate = LocalDate.of(2024, 1, 15);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 15), 7L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", singleDate,
                    singleDate))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname,
                    singleDate,
                    singleDate);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 15), 7L)
                );
                assertThat(result).isEqualTo(expected);

                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", singleDate, singleDate);
            }

            @Test
            @DisplayName("날짜 순서가 잘못된 경우 (from > to)")
            void getUserStreak_InvalidDateRange() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate from = LocalDate.of(2024, 1, 31);
                LocalDate to = LocalDate.of(2024, 1, 1);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);
                List<DailyHistoryResponseDto> emptyResults = List.of();

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", from, to))
                    .willReturn(emptyResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname, from,
                    to);

                // then
                assertThat(result).isEmpty();
                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", from, to);
            }

            @Test
            @DisplayName("연속되지 않은 날짜의 데이터 조회")
            void getUserStreak_NonConsecutiveDates() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 10);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                // 1일, 5일, 9일에만 문제를 해결
                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 2L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 5), 4L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 9), 1L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname, from,
                    to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 2L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 5), 4L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 9), 1L)
                );
                assertThat(result).isEqualTo(expected);

                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", from, to);
            }

            @Test
            @DisplayName("null 파라미터로 조회하는 경우")
            void getUserStreak_WithNullParameters() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate from = null;
                LocalDate to = null;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 3L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname, from,
                    to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 3L)
                );
                assertThat(result).isEqualTo(expected);

                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", null, null);
            }

            @Test
            @DisplayName("from만 null인 경우")
            void getUserStreak_WithFromNull() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate from = null;
                LocalDate to = LocalDate.of(2024, 1, 31);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 30), 2L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 31), 4L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname, from,
                    to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 30), 2L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 31), 4L)
                );
                assertThat(result).isEqualTo(expected);

                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", null, to);
            }

            @Test
            @DisplayName("to만 null인 경우")
            void getUserStreak_WithToNull() {
                // given
                setupAcceptedComcode();
                String nickname = "testUser";
                Long userId = 1L;
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = null;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 2, 1), 5L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(submissionRepository.getUserDateSolvedCount(userId, "ACCEPTED", from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserStreak(nickname, from,
                    to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 2, 1), 5L)
                );
                assertThat(result).isEqualTo(expected);

                then(submissionRepository).should()
                    .getUserDateSolvedCount(userId, "ACCEPTED", from, null);
            }
        }

        @Nested
        @DisplayName("사용자 일별 히스토리 조회")
        class GetUserDailyHistory {

            @Test
            @DisplayName("사용자의 레이팅 히스토리를 정상 조회")
            void getUserDailyHistory_Success() {
                // given
                setupRatingComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "RATING";
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 3);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1200L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 1250L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 3), 1300L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, from, to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1200L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 1250L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 3), 1300L)
                );
                assertThat(result).isEqualTo(expected);

                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, from, to);
            }

            @Test
            @DisplayName("존재하지 않는 닉네임으로 히스토리 조회")
            void getUserDailyHistory_UserNotFound() {
                // given
                String nickname = "nonexistentUser";
                String criteria = "RANKING";
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 31);

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(
                    () -> userService.getUserDailyHistory(nickname, criteria, from, to))
                    .isInstanceOf(UserNotFoundException.class);

                then(userRankingHistoryRepository).should(never())
                    .getUserDailyHistory(any(), any(), any(), any());
            }

            @Test
            @DisplayName("해당 기간에 히스토리가 없는 경우")
            void getUserDailyHistory_NoHistory() {
                // given
                setupSolvedCountComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "SOLVED_COUNT";
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 31);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);
                List<DailyHistoryResponseDto> emptyResults = List.of();

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                    .willReturn(emptyResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, from, to);

                // then
                assertThat(result).isEmpty();
                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, from, to);
            }

            @Test
            @DisplayName("다양한 criteria 타입으로 조회")
            void getUserDailyHistory_DifferentCriteria() {
                // given
                setupRankingComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "RANKING";
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 2);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 100L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 95L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, from, to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 100L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 95L)
                );
                assertThat(result).isEqualTo(expected);

                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, from, to);
            }

            @Test
            @DisplayName("null 파라미터로 조회하는 경우")
            void getUserDailyHistory_WithNullParameters() {
                // given
                setupRatingComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "RATING";
                LocalDate from = null;
                LocalDate to = null;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1000L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 1050L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, from, to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1000L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 2), 1050L)
                );
                assertThat(result).isEqualTo(expected);

                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, null, null);
            }

            @Test
            @DisplayName("하루만 조회하는 경우")
            void getUserDailyHistory_SingleDay() {
                // given
                setupRatingComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "RATING";
                LocalDate singleDate = LocalDate.of(2024, 1, 15);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 15), 1400L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, singleDate,
                    singleDate))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, singleDate, singleDate);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 15), 1400L)
                );
                assertThat(result).isEqualTo(expected);

                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, singleDate, singleDate);
            }

            @Test
            @DisplayName("연속되지 않은 날짜의 히스토리 데이터 조회")
            void getUserDailyHistory_NonConsecutiveDates() {
                // given
                setupSolvedCountComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "SOLVED_COUNT";
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = LocalDate.of(2024, 1, 10);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                // 1일, 5일, 9일에만 히스토리가 있음
                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 10L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 5), 15L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 9), 20L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, from, to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 10L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 5), 15L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 9), 20L)
                );
                assertThat(result).isEqualTo(expected);

                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, from, to);
            }

            @Test
            @DisplayName("from만 null인 경우")
            void getUserDailyHistory_WithFromNull() {
                // given
                setupRankingComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "RANKING";
                LocalDate from = null;
                LocalDate to = LocalDate.of(2024, 1, 31);

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 30), 50L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 31), 45L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, from, to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 30), 50L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 31), 45L)
                );
                assertThat(result).isEqualTo(expected);

                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, null, to);
            }

            @Test
            @DisplayName("to만 null인 경우")
            void getUserDailyHistory_WithToNull() {
                // given
                setupRatingComcode();
                String nickname = "testUser";
                Long userId = 1L;
                String criteria = "RATING";
                LocalDate from = LocalDate.of(2024, 1, 1);
                LocalDate to = null;

                UserAccountEntity userAccount = UserFixture.createUserAccountEntity(userId,
                    nickname);

                List<DailyHistoryResponseDto> queryResults = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1000L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 2, 1), 1100L)
                );

                given(userAccountRepository.findByNickname(nickname))
                    .willReturn(Optional.of(userAccount));
                given(userRankingHistoryRepository.getUserDailyHistory(userId, criteria, from, to))
                    .willReturn(queryResults);

                // when
                List<DailyHistoryResponseDto> result = userService.getUserDailyHistory(nickname,
                    criteria, from, to);

                // then
                List<DailyHistoryResponseDto> expected = List.of(
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 1, 1), 1000L),
                    UserFixture.createDailyHistoryResponseDto(LocalDate.of(2024, 2, 1), 1100L)
                );
                assertThat(result).isEqualTo(expected);

                then(userRankingHistoryRepository).should()
                    .getUserDailyHistory(userId, criteria, from, null);
            }
        }
    }
}