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
import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.gym.enums.GymTierType;
import com.climbx.climbx.gym.repository.GymAreaRepository;
import com.climbx.climbx.problem.dto.ProblemCreateRequestDto;
import com.climbx.climbx.problem.dto.ProblemCreateResponseDto;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.enums.HoldColorType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.problem.exception.GymAreaNotFoundException;
import com.climbx.climbx.problem.repository.ContributionRepository;
import com.climbx.climbx.problem.repository.ProblemRepository;
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

            given(gymAreaRepository.findById(gymAreaId))
                .willReturn(Optional.of(gymAreaEntity));
            given(s3Service.uploadProblemImage(any(), eq(gymAreaId), any()))
                .willReturn(expectedCdnUrl);
            given(problemRepository.save(any(ProblemEntity.class)))
                .willReturn(savedProblem);

            // when
            ProblemCreateResponseDto result = problemService.registerProblem(request, problemImage);

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
        @DisplayName("이미지 없이 문제를 생성하면, S3 업로드 없이 문제를 저장한다")
        void createProblemWithoutImage() {
            // given
            Long gymAreaId = 1L;
            GymTierType localLevel = GymTierType.BLUE;
            HoldColorType holdColor = HoldColorType.BLUE;
            Integer problemRating = 1600;
            UUID problemId = UUID.randomUUID();

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

            ProblemEntity savedProblem = ProblemEntity.builder()
                .problemId(problemId)
                .gymEntity(gymEntity)
                .gymArea(gymAreaEntity)
                .localLevel(localLevel)
                .holdColor(holdColor)
                .rating(problemRating)
                .problemImageCdnUrl(null)
                .activeStatus(ActiveStatusType.ACTIVE)
                .build();

            given(gymAreaRepository.findById(gymAreaId))
                .willReturn(Optional.of(gymAreaEntity));
            given(problemRepository.save(any(ProblemEntity.class)))
                .willReturn(savedProblem);

            // when
            MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "test".getBytes());
            ProblemCreateResponseDto result = problemService.registerProblem(request,
                multipartFile);

            // then
            then(gymAreaRepository).should(times(1)).findById(gymAreaId);
            then(s3Service).should(times(0)).uploadProblemImage(eq(problemId), anyLong(), any());
            then(problemRepository).should(times(1)).save(any(ProblemEntity.class));

            assertThat(result.problemId()).isEqualTo(problemId);
            assertThat(result.gymAreaId()).isEqualTo(gymAreaId);
            assertThat(result.localLevel()).isEqualTo(localLevel);
            assertThat(result.holdColor()).isEqualTo(holdColor);
            assertThat(result.problemRating()).isEqualTo(problemRating);
            assertThat(result.problemImageCdnUrl()).isNull();
            assertThat(result.activeStatus()).isEqualTo(ActiveStatusType.ACTIVE);
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
            assertThatThrownBy(() -> problemService.registerProblem(request, null))
                .isInstanceOf(GymAreaNotFoundException.class);

            then(gymAreaRepository).should(times(1)).findById(nonExistentGymAreaId);
            then(s3Service).should(times(0)).uploadProblemImage(any(), anyLong(), any());
            then(problemRepository).should(times(0)).save(any(ProblemEntity.class));
        }
    }
} 