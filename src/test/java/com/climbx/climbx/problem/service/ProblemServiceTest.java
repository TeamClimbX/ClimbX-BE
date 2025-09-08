package com.climbx.climbx.problem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.fixture.GymAreaFixture;
import com.climbx.climbx.fixture.GymFixture;
import com.climbx.climbx.fixture.ProblemFixture;
import com.climbx.climbx.fixture.UserFixture;
import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.gym.enums.GymTierType;
import com.climbx.climbx.gym.repository.GymAreaRepository;
import com.climbx.climbx.problem.dto.ContributionResponseDto;
import com.climbx.climbx.problem.dto.ProblemCreateRequestDto;
import com.climbx.climbx.problem.dto.ProblemCreateResponseDto;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.entity.ContributionEntity;
import com.climbx.climbx.problem.entity.ContributionTagEntity;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.enums.HoldColorType;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.problem.exception.GymAreaNotFoundException;
import com.climbx.climbx.common.exception.InvalidParameterException;
import com.climbx.climbx.problem.repository.ContributionRepository;
import com.climbx.climbx.problem.repository.ProblemRepository;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.enums.UserTierType;
import com.climbx.climbx.user.service.UserLookupService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private GymAreaRepository gymAreaRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private ContributionRepository contributionRepository;
    @Mock
    private UserLookupService userLookUpService;

    @InjectMocks
    private ProblemService problemService;

    @Nested
    @DisplayName("문제 검색 조건별 테스트")
    class GetProblemSpotsWithFilters {

        @Test
        @DisplayName("gymId, localLevel, holdColor 모든 조건이 주어지면, 해당 조건에 맞는 문제들을 spotId로 그룹화하여 반환한다")
        void getProblemSpotsWithAllFilters() {
            // given
            Long gymId = 1L;
            GymTierType localLevel = GymTierType.RED;
            HoldColorType holdColor = HoldColorType.BLUE;
            Long gymAreaId = 1L;
            UUID problemId1 = UUID.randomUUID();
            UUID problemId2 = UUID.randomUUID();
            UUID problemId3 = UUID.randomUUID();

            GymEntity gymEntity = GymFixture.createGymEntity(gymId, "테스트 클라이밍장", 37.0, 126.0);

            GymAreaEntity gymArea1 = GymAreaFixture.createGymAreaEntity(1L, gymEntity, "테스트 구역");
            GymAreaEntity gymArea2 = GymAreaFixture.createGymAreaEntity(2L, gymEntity, "테스트 구역");
            GymAreaEntity gymArea3 = GymAreaFixture.createGymAreaEntity(3L, gymEntity, "테스트 구역");

            ProblemEntity problemEntity1 = ProblemFixture.createProblemEntity(problemId1, gymEntity,
                gymArea1, localLevel, holdColor, 1200);
            ProblemEntity problemEntity2 = ProblemFixture.createProblemEntity(problemId2, gymEntity,
                gymArea2, localLevel, holdColor, 1300);
            ProblemEntity problemEntity3 = ProblemFixture.createProblemEntity(problemId3, gymEntity,
                gymArea3, localLevel, holdColor, 1400);

            ProblemInfoResponseDto problemInfDto1 = ProblemInfoResponseDto.from(problemEntity1,
                gymEntity,
                gymArea1);
            ProblemInfoResponseDto problemInfDto2 = ProblemInfoResponseDto.from(problemEntity2,
                gymEntity,
                gymArea2);
            ProblemInfoResponseDto problemInfDto3 = ProblemInfoResponseDto.from(problemEntity3,
                gymEntity,
                gymArea3);

            List<ProblemInfoResponseDto> mockProblems = List.of(problemInfDto1, problemInfDto2,
                problemInfDto3);

            given(problemRepository.findByGymAndAreaAndLevelAndColorAndProblemTierAndActiveStatus(
                gymId, gymAreaId, localLevel, holdColor, ProblemTierType.D1, ActiveStatusType.ACTIVE
            )).willReturn(mockProblems);

            // when
            List<ProblemInfoResponseDto> result = problemService.getProblemsWithFilters(
                gymId, gymAreaId, localLevel, holdColor, ProblemTierType.D1,
                ActiveStatusType.ACTIVE);

            // then
            then(problemRepository).should(times(1))
                .findByGymAndAreaAndLevelAndColorAndProblemTierAndActiveStatus(gymId, gymAreaId,
                    localLevel, holdColor, ProblemTierType.D1, ActiveStatusType.ACTIVE);

            assertThat(result).hasSize(3); // 모든 문제가 하나의 그룹으로
        }
    }

    @Nested
    @DisplayName("문제 생성 테스트")
    class CreateProblem {

        @Test
        @DisplayName("이미지와 함께 문제를 생성하면, S3에 이미지를 업로드하고 CDN URL과 함께 문제를 저장한다")
        void createProblemWithImage() {
            // given
            Long gymAreaId = 1L;
            GymTierType localLevel = GymTierType.GREEN;
            HoldColorType holdColor = HoldColorType.RED;
            Integer rating = 1500;
            UUID problemId = UUID.randomUUID();

            ProblemCreateRequestDto request = ProblemCreateRequestDto.builder()
                .gymAreaId(gymAreaId)
                .localLevel(localLevel)
                .holdColor(holdColor)
                .build();

            MockMultipartFile problemImage = new MockMultipartFile(
                "problemImage",
                "test-problem.jpg",
                "image/jpeg",
                "test image content".getBytes()
            );

            GymEntity gymEntity = GymFixture.createGymEntity(1L, "테스트 클라이밍장", 37.0, 126.0);
            GymAreaEntity gymAreaEntity = GymAreaEntity.builder()
                .gymAreaId(gymAreaId)
                .gym(gymEntity)
                .areaImageCdnUrl("https://cdn.example.com/area-image.jpg")
                .build();

            String expectedCdnUrl = "https://cdn.example.com/problem-images/1_1640995200000.jpg";

            ProblemEntity savedProblem = ProblemEntity.builder()
                .problemId(problemId)
                .gymEntity(gymEntity)
                .gymArea(gymAreaEntity)
                .localLevel(localLevel)
                .holdColor(holdColor)
                .rating(rating)
                .problemImageCdnUrl(expectedCdnUrl)
                .activeStatus(ActiveStatusType.ACTIVE)
                .build();

            UserStatEntity userStatEntity = UserStatEntity.builder().build();
            UserAccountEntity userAccountEntity = UserAccountEntity.builder()
                .userStatEntity(userStatEntity)
                .build();
            
            given(gymAreaRepository.findById(gymAreaId))
                .willReturn(Optional.of(gymAreaEntity));
            given(s3Service.uploadProblemImage(any(), eq(gymAreaId), any()))
                .willReturn(expectedCdnUrl);
            given(problemRepository.save(any(ProblemEntity.class)))
                .willReturn(savedProblem);
            given(contributionRepository.saveAll(any()))
                .willReturn(List.of());
            given(userLookUpService.findUserById(eq(1L)))
                .willReturn(userAccountEntity);

            // when
            Long userId = 1L;
            ProblemCreateResponseDto result = problemService.registerProblem(userId, request, problemImage);

            // then
            then(gymAreaRepository).should(times(1)).findById(gymAreaId);
            then(s3Service).should(times(1))
                .uploadProblemImage(any(), eq(gymAreaId), eq(problemImage));
            then(problemRepository).should(times(1)).save(any(ProblemEntity.class));

            assertThat(result.problemId()).isEqualTo(problemId);
            assertThat(result.gymAreaId()).isEqualTo(gymAreaId);
            assertThat(result.localLevel()).isEqualTo(localLevel);
            assertThat(result.holdColor()).isEqualTo(holdColor);
            assertThat(result.problemRating()).isEqualTo(rating);
            assertThat(result.problemImageCdnUrl()).isEqualTo(expectedCdnUrl);
            assertThat(result.activeStatus()).isEqualTo(ActiveStatusType.ACTIVE);
        }

        @Test
        @DisplayName("이미지 없이 문제를 생성하면, InvalidParameterException이 발생한다")
        void createProblemWithoutImageShouldThrowException() {
            // given
            Long gymAreaId = 1L;
            GymTierType localLevel = GymTierType.BLUE;
            HoldColorType holdColor = HoldColorType.BLUE;

            ProblemCreateRequestDto request = ProblemCreateRequestDto.builder()
                .gymAreaId(gymAreaId)
                .localLevel(localLevel)
                .holdColor(holdColor)
                .build();

            GymEntity gymEntity = GymFixture.createGymEntity(1L, "테스트 클라이밍장", 37.0, 126.0);
            GymAreaEntity gymAreaEntity = GymAreaEntity.builder()
                .gymAreaId(gymAreaId)
                .gym(gymEntity)
                .areaImageCdnUrl("https://cdn.example.com/area-image.jpg")
                .build();

            given(gymAreaRepository.findById(gymAreaId))
                .willReturn(Optional.of(gymAreaEntity));

            // when & then
            Long userId = 1L;
            MockMultipartFile emptyFile = new MockMultipartFile("file", "test.txt",
                "text/plain", new byte[0]); // empty file
            
            assertThatThrownBy(() -> problemService.registerProblem(userId, request, emptyFile))
                .isInstanceOf(InvalidParameterException.class);

            then(gymAreaRepository).should(times(1)).findById(gymAreaId);
            then(s3Service).should(times(0)).uploadProblemImage(any(), anyLong(), any());
            then(problemRepository).should(times(0)).save(any(ProblemEntity.class));
        }

        @Test
        @DisplayName("존재하지 않는 gymAreaId로 문제 생성을 시도하면 GymAreaNotFoundException이 발생한다")
        void createProblemWithNonExistentGymArea() {
            // given
            Long nonExistentGymAreaId = 999L;
            ProblemCreateRequestDto request = ProblemCreateRequestDto.builder()
                .gymAreaId(nonExistentGymAreaId)
                .localLevel(GymTierType.RED)
                .holdColor(HoldColorType.GREEN)
                .build();

            given(gymAreaRepository.findById(nonExistentGymAreaId))
                .willReturn(Optional.empty());

            // when & then
            Long userId = 1L;
            assertThatThrownBy(() -> problemService.registerProblem(userId, request, null))
                .isInstanceOf(GymAreaNotFoundException.class);

            then(gymAreaRepository).should(times(1)).findById(nonExistentGymAreaId);
            then(s3Service).should(times(0)).uploadProblemImage(any(), anyLong(), any());
            then(problemRepository).should(times(0)).save(any(ProblemEntity.class));
        }
    }

    @Nested
    @DisplayName("문제 투표 조회 테스트")
    class GetProblemVotes {

        // Helper method to create ContributionEntity
        private ContributionEntity createContributionEntity(
            Long contributionId,
            UserAccountEntity userAccountEntity,
            ProblemEntity problemEntity,
            ProblemTierType problemTier,
            List<ProblemTagType> tags,
            String comment
        ) {
            List<ContributionTagEntity> contributionTags = tags.stream()
                .map(tag -> ContributionTagEntity.builder()
                    .tag(tag)
                    .build())
                .toList();

            return ContributionEntity.builder()
                .contributionId(contributionId)
                .userAccountEntity(userAccountEntity)
                .problemEntity(problemEntity)
                .tier(problemTier)
                .comment(comment)
                .contributionTags(contributionTags)
                .build();
        }

        @Test
        @DisplayName("사용자 투표가 있을 때 프로필 사진과 티어 정보를 포함하여 반환한다")
        void shouldReturnVotesWithUserProfileAndTier() {
            // given
            UUID problemId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            // Create test users with different ratings
            UserAccountEntity user1 = UserFixture.createUserAccountEntity(1L, "user1", 
                "status1", "http://example.com/profile1.jpg");
            UserStatEntity userStat1 = UserFixture.createUserStatEntity(1L, 1500); // P2 tier
            user1 = UserAccountEntity.builder()
                .userId(user1.userId())
                .nickname(user1.nickname())
                .statusMessage(user1.statusMessage())
                .profileImageCdnUrl(user1.profileImageCdnUrl())
                .role(user1.role())
                .userStatEntity(userStat1)
                .build();

            UserAccountEntity user2 = UserFixture.createUserAccountEntity(2L, "user2",
                "status2", "http://example.com/profile2.jpg");
            UserStatEntity userStat2 = UserFixture.createUserStatEntity(2L, 1200); // G1 tier
            user2 = UserAccountEntity.builder()
                .userId(user2.userId())
                .nickname(user2.nickname())
                .statusMessage(user2.statusMessage())
                .profileImageCdnUrl(user2.profileImageCdnUrl())
                .role(user2.role())
                .userStatEntity(userStat2)
                .build();

            // Create problem entity
            GymEntity gym = GymFixture.createGymEntity(1L, "Test Gym", 37.0, 126.0);
            GymAreaEntity gymArea = GymAreaFixture.createGymAreaEntity(1L, gym, "Test Area");
            ProblemEntity problem = ProblemFixture.createProblemEntity(problemId, gym, gymArea);

            // Create contribution entities
            List<ContributionEntity> contributions = List.of(
                createContributionEntity(1L, user1, problem, ProblemTierType.P2,
                    List.of(ProblemTagType.BALANCE, ProblemTagType.LUNGE), "Great problem!"),
                createContributionEntity(2L, user2, problem, ProblemTierType.G1,
                    List.of(ProblemTagType.CRIMP_HOLD), "Challenging route!")
            );

            given(contributionRepository.findRecentUserVotes(problemId, pageable))
                .willReturn(contributions);

            // when
            List<ContributionResponseDto> result = problemService.getProblemVotes(problemId, pageable);

            // then
            assertThat(result).hasSize(2);
            
            // Verify first contribution
            ContributionResponseDto firstVote = result.get(0);
            assertThat(firstVote.nickname()).isEqualTo("user1");
            assertThat(firstVote.profileImageCdnUrl()).isEqualTo("http://example.com/profile1.jpg");
            assertThat(firstVote.userTier()).isEqualTo(UserTierType.P2); // 1500 rating -> P2
            assertThat(firstVote.problemTier()).isEqualTo(ProblemTierType.P2);
            assertThat(firstVote.tags()).containsExactly(ProblemTagType.BALANCE, ProblemTagType.LUNGE);
            assertThat(firstVote.comment()).isEqualTo("Great problem!");

            // Verify second contribution
            ContributionResponseDto secondVote = result.get(1);
            assertThat(secondVote.nickname()).isEqualTo("user2");
            assertThat(secondVote.profileImageCdnUrl()).isEqualTo("http://example.com/profile2.jpg");
            assertThat(secondVote.userTier()).isEqualTo(UserTierType.G1); // 1200 rating -> G1
            assertThat(secondVote.problemTier()).isEqualTo(ProblemTierType.G1);
            assertThat(secondVote.tags()).containsExactly(ProblemTagType.CRIMP_HOLD);
            assertThat(secondVote.comment()).isEqualTo("Challenging route!");

            // Verify repository method was called
            then(contributionRepository).should().findRecentUserVotes(problemId, pageable);
        }

        @Test
        @DisplayName("사용자 투표가 없을 때 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoVotes() {
            // given
            UUID problemId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            given(contributionRepository.findRecentUserVotes(problemId, pageable))
                .willReturn(List.of());

            // when
            List<ContributionResponseDto> result = problemService.getProblemVotes(problemId, pageable);

            // then
            assertThat(result).isEmpty();
            then(contributionRepository).should().findRecentUserVotes(problemId, pageable);
        }

        @Test
        @DisplayName("페이징 파라미터가 올바르게 전달된다")
        void shouldPassPagingParametersCorrectly() {
            // given
            UUID problemId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(1, 5); // Second page, 5 items per page

            given(contributionRepository.findRecentUserVotes(problemId, pageable))
                .willReturn(List.of());

            // when
            List<ContributionResponseDto> result = problemService.getProblemVotes(problemId, pageable);

            // then
            assertThat(result).isEmpty();
            then(contributionRepository).should().findRecentUserVotes(eq(problemId), eq(pageable));
        }
    }
} 