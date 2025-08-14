package com.climbx.climbx.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.climbx.climbx.fixture.GymFixture;
import com.climbx.climbx.gym.dto.GymInfoResponseDto;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.gym.exception.GymNotFoundException;
import com.climbx.climbx.gym.repository.GymRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GymServiceTest {

    @Mock
    private GymRepository gymRepository;

    @InjectMocks
    private GymService gymService;

    @Nested
    @DisplayName("클라이밍장 단일 조회")
    class GetGymDetails {

        @Test
        @DisplayName("존재하는 클라이밍장 ID가 주어지면, 해당 클라이밍장 정보를 반환한다")
        void getGymById_whenGymExists() {
            // given
            Long gymId = 1L;
            GymEntity gymEntity = GymFixture.createGymEntity(gymId, "Test Gym", 37.0, 126.0);
            GymInfoResponseDto expectedDto = GymInfoResponseDto.from(gymEntity);

            given(gymRepository.findById(gymId))
                .willReturn(Optional.of(gymEntity));

            // when
            GymInfoResponseDto result = gymService.getGymById(gymId);

            // then
            then(gymRepository).should(times(1)).findById(gymId);
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("존재하지 않는 클라이밍장 ID가 주어지면, GymNotFoundException을 발생시킨다")
        void throwGymNotFoundException_whenGymDoesNotExist() {
            // given
            Long gymId = 999L;

            given(gymRepository.findById(gymId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> gymService.getGymById(gymId))
                .isInstanceOf(GymNotFoundException.class);

            then(gymRepository).should(times(1)).findById(gymId);
        }
    }

    @Nested
    @DisplayName("클라이밍장 조회")
    class GetGymList {

        @Test
        @DisplayName("위치 정보가 주어지지 않았을 때, 전체 클라이밍장 목록을 반환한다")
        void getGymList() {
            // given
            GymEntity gymEntity1 = GymFixture.createGymEntity(1L, "Gym1", 37.0, 126.0);
            GymEntity gymEntity2 = GymFixture.createGymEntity(2L, "Gym2", 38.0, 127.0);

            given(gymRepository.findAll())
                .willReturn(List.of(gymEntity1, gymEntity2));

            // when
            List<GymInfoResponseDto> gymList = gymService.getGymList(null);

            // then
            then(gymRepository).should(times(1)).findAll();
            assertThat(gymList.size()).isEqualTo(2);
            assertThat(gymList.get(0)).isEqualTo(
                GymFixture.createGymInfoResponseDto(1L, "Gym1", 37.0, 126.0));
            assertThat(gymList.get(1)).isEqualTo(
                GymFixture.createGymInfoResponseDto(2L, "Gym2", 38.0, 127.0));
        }

        @DisplayName("전달받은 위치 정보가 유효할 때, 현재 위치와 가까운 순으로 클라이밍장 목록을 반환한다")
        @ParameterizedTest
        @CsvSource({
            // 위도, 경도, 가까운 클라이밍장 ID, 먼 클라이밍장 ID
            "1.0,   50.0,  1,  2", // 첫 번째가 가까운 경우
            "80.0,  170.0,  2,  1"  // 두 번째가 가까운 경우
        })
        void getGymList_whenValidLocation(Double latitude, Double longitude,
            Long firstId, Long secondId) {
            // given
            GymEntity gymEntity1 = GymFixture.createGymEntity(1L, "Gym1", 37.0, 126.0);
            GymEntity gymEntity2 = GymFixture.createGymEntity(2L, "Gym2", 38.0, 127.0);

            // gymEntity1과 gymEntity2의 거리를 기준으로 정렬
            List<GymEntity> sortedGyms = firstId == 1L
                ? List.of(gymEntity1, gymEntity2)
                : List.of(gymEntity2, gymEntity1);

            given(gymRepository.findAllByLocationOrderByDistance(latitude, longitude))
                .willReturn(sortedGyms);

            // when
            List<GymInfoResponseDto> result = gymService.getGymListByDistance(
                latitude, longitude, null
            );

            // then
            then(gymRepository)
                .should(times(1))
                .findAllByLocationOrderByDistance(latitude, longitude);
            assertThat(result).hasSize(2);
            assertThat(result.get(0).gymId()).isEqualTo(firstId);
            assertThat(result.get(1).gymId()).isEqualTo(secondId);
        }

        @DisplayName("keyword가 존재하고 좌표 정보가 없으면, 해당 클라이밍장 목록 전체를 반환한다")
        @Test
        void getGymList_whenKeywordIsContainingAndNoCoordinates() {
            // given
            GymEntity gymEntity1 = GymFixture.createGymEntity(1L, "더클라임 클라이밍 홍대", 37.0, 126.0);
            GymEntity gymEntity2 = GymFixture.createGymEntity(2L, "클라이밍존 홍대", 37.0, 126.0);
            List<GymEntity> mockGyms = List.of(gymEntity1, gymEntity2);

            String keyword = "홍대";
            given(gymRepository.findAllByNameContainingIgnoreCase(keyword))
                .willReturn(mockGyms);

            // when
            List<GymInfoResponseDto> result = gymService.getGymList(keyword);

            // then
            then(gymRepository).should().findAllByNameContainingIgnoreCase(keyword);

            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(GymInfoResponseDto::name)
                .containsExactlyInAnyOrder("더클라임 클라이밍 홍대", "클라이밍존 홍대");
        }

        @DisplayName("keyword가 존재하고 좌표 정보가 있으면, 해당 클라이밍장 목록을 거리순으로 정렬하여 반환한다")
        @Test
        void getGymList_whenKeywordIsContainingAndCoordinates() {
            // given
            GymEntity gymEntity1 = GymFixture.createGymEntity(1L, "더클라임 클라이밍 홍대", 37.0, 126.0);
            GymEntity gymEntity2 = GymFixture.createGymEntity(2L, "클라이밍존 홍대", 37.0, 126.0);
            List<GymEntity> mockGyms = List.of(gymEntity1, gymEntity2);

            Double latitude = 37.0;
            Double longitude = 126.0;
            String keyword = "홍대";

            given(gymRepository.findAllByNameContainingIgnoreCaseOrderByDistance(
                latitude, longitude, keyword)
            ).willReturn(mockGyms);

            // when
            List<GymInfoResponseDto> result = gymService.getGymListByDistance(
                latitude, longitude, keyword
            );

            // then
            then(gymRepository).should()
                .findAllByNameContainingIgnoreCaseOrderByDistance(latitude, longitude, keyword);
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(GymInfoResponseDto::name)
                .containsExactlyInAnyOrder("더클라임 클라이밍 홍대", "클라이밍존 홍대");
        }
    }
}
