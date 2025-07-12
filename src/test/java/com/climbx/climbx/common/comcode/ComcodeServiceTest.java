package com.climbx.climbx.common.comcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.climbx.climbx.common.comcode.dto.ComcodeDto;
import com.climbx.climbx.common.comcode.entity.ComcodeEntity;
import com.climbx.climbx.common.comcode.exception.ComcodeNotFound;
import com.climbx.climbx.common.comcode.repository.ComcodeRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComcodeServiceTest {

    @Mock
    private ComcodeRepository comcodeRepository;

    private ComcodeService comcodeService;

    private List<ComcodeEntity> mockComcodeEntities;

    @BeforeEach
    void setUp() {
        // Mock 데이터 준비
        mockComcodeEntities = List.of(
            createComcodeEntity("ROLE", "USER", "일반 사용자", "일반 사용자 권한", 1),
            createComcodeEntity("ROLE", "ADMIN", "관리자", "관리자 권한", 2),
            createComcodeEntity("STATUS", "PENDING", "대기중", "제출 대기 상태", 1),
            createComcodeEntity("STATUS", "ACCEPTED", "승인됨", "제출 승인 상태", 2),
            createComcodeEntity("STATUS", "REJECTED", "거부됨", "제출 거부 상태", 3),
            createComcodeEntity("USER_HISTORY_CRITERIA", "RATING", "레이팅", "사용자 레이팅", 1),
            createComcodeEntity("USER_HISTORY_CRITERIA", "RANKING", "랭킹", "사용자 랭킹", 2),
            createComcodeEntity("USER_HISTORY_CRITERIA", "SOLVED_COUNT", "해결 문제 수", "해결한 문제 개수", 3)
        );

        given(comcodeRepository.findAll()).willReturn(mockComcodeEntities);

        // ComcodeService 인스턴스 생성 (생성자에서 getCodes() 호출됨)
        comcodeService = new ComcodeService(comcodeRepository);
        comcodeService.init();
    }

    private ComcodeEntity createComcodeEntity(String groupCode, String code, String codeName,
        String description, int sortOrder) {
        return ComcodeEntity.builder()
            .groupCode(groupCode)
            .code(code)
            .codeName(codeName)
            .description(description)
            .sortOrder(sortOrder)
            .build();
    }

    @Nested
    @DisplayName("getCodes 메서드 테스트")
    class GetCodes {

        @Test
        @DisplayName("모든 코드를 Map으로 정상 반환")
        void getCodes_Success() {
            // when
            Map<String, ComcodeDto> result = comcodeService.getCodes();

            // then
            assertThat(result).hasSize(8);
            assertThat(result).containsKeys("USER", "ADMIN", "PENDING", "ACCEPTED", "REJECTED",
                "RATING", "RANKING", "SOLVED_COUNT");

            // 특정 코드 검증
            ComcodeDto userCode = result.get("USER");
            assertThat(userCode.codeGroup()).isEqualTo("ROLE");
            assertThat(userCode.code()).isEqualTo("USER");
            assertThat(userCode.codeName()).isEqualTo("일반 사용자");
            assertThat(userCode.sortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 Repository일 때 빈 Map 반환")
        void getCodes_EmptyRepository() {
            // given
            given(comcodeRepository.findAll()).willReturn(List.of());
            ComcodeService emptyService = new ComcodeService(comcodeRepository);

            // when
            Map<String, ComcodeDto> result = emptyService.getCodes();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCodesByGroup 메서드 테스트")
    class GetCodesByGroup {

        @Test
        @DisplayName("ROLE 그룹의 코드들을 정상 반환")
        void getCodesByGroup_RoleGroup_Success() {
            // when
            List<ComcodeDto> result = comcodeService.getCodesByGroup("ROLE");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ComcodeDto::code)
                .containsExactlyInAnyOrder("USER", "ADMIN");
            assertThat(result).extracting(ComcodeDto::codeGroup)
                .allMatch(group -> group.equals("ROLE"));
        }

        @Test
        @DisplayName("STATUS 그룹의 코드들을 정상 반환")
        void getCodesByGroup_SubmissionStatusGroup_Success() {
            // when
            List<ComcodeDto> result = comcodeService.getCodesByGroup("STATUS");

            // then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(ComcodeDto::code)
                .containsExactlyInAnyOrder("PENDING", "ACCEPTED", "REJECTED");
            assertThat(result).extracting(ComcodeDto::codeGroup)
                .allMatch(group -> group.equals("STATUS"));
        }

        @Test
        @DisplayName("USER_HISTORY_CRITERIA 그룹의 코드들을 정상 반환")
        void getCodesByGroup_UserHistoryCriteriaGroup_Success() {
            // when
            List<ComcodeDto> result = comcodeService.getCodesByGroup("USER_HISTORY_CRITERIA");

            // then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(ComcodeDto::code)
                .containsExactlyInAnyOrder("RATING", "RANKING", "SOLVED_COUNT");
            assertThat(result).extracting(ComcodeDto::codeGroup)
                .allMatch(group -> group.equals("USER_HISTORY_CRITERIA"));
        }

        @Test
        @DisplayName("존재하지 않는 그룹일 때 빈 리스트 반환")
        void getCodesByGroup_NonExistentGroup_EmptyList() {
            // when
            List<ComcodeDto> result = comcodeService.getCodesByGroup("NON_EXISTENT_GROUP");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 그룹일 때 빈 리스트 반환")
        void getCodesByGroup_NullGroup_EmptyList() {
            // when
            List<ComcodeDto> result = comcodeService.getCodesByGroup(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 그룹일 때 빈 리스트 반환")
        void getCodesByGroup_EmptyGroup_EmptyList() {
            // when
            List<ComcodeDto> result = comcodeService.getCodesByGroup("");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCode 메서드 테스트")
    class GetCode {

        @Test
        @DisplayName("존재하는 코드를 정상 반환 - USER")
        void getCode_ExistingCode_User_Success() {
            // when
            ComcodeDto result = comcodeService.getCodeDto("USER");

            // then
            assertThat(result.codeGroup()).isEqualTo("ROLE");
            assertThat(result.code()).isEqualTo("USER");
            assertThat(result.codeName()).isEqualTo("일반 사용자");
            assertThat(result.sortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하는 코드를 정상 반환 - PENDING")
        void getCode_ExistingCode_Pending_Success() {
            // when
            ComcodeDto result = comcodeService.getCodeDto("PENDING");

            // then
            assertThat(result.codeGroup()).isEqualTo("STATUS");

            // then
            assertThat(result.codeGroup()).isEqualTo("STATUS");
            assertThat(result.code()).isEqualTo("PENDING");
            assertThat(result.codeName()).isEqualTo("대기중");
            assertThat(result.sortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하는 코드를 정상 반환 - RATING")
        void getCode_ExistingCode_Rating_Success() {
            // when
            ComcodeDto result = comcodeService.getCodeDto("RATING");

            // then
            assertThat(result.codeGroup()).isEqualTo("USER_HISTORY_CRITERIA");
            assertThat(result.code()).isEqualTo("RATING");
            assertThat(result.codeName()).isEqualTo("레이팅");
            assertThat(result.sortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 코드일 때 ComcodeNotFound 예외 발생")
        void getCode_NonExistentCode_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> comcodeService.getCodeDto("NON_EXISTENT_CODE"))
                .isInstanceOf(ComcodeNotFound.class);
        }

        @Test
        @DisplayName("null 코드일 때 ComcodeNotFound 예외 발생")
        void getCode_NullCode_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> comcodeService.getCodeDto(null))
                .isInstanceOf(ComcodeNotFound.class);
        }

        @Test
        @DisplayName("빈 문자열 코드일 때 ComcodeNotFound 예외 발생")
        void getCode_EmptyCode_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> comcodeService.getCodeDto(""))
                .isInstanceOf(ComcodeNotFound.class);
        }
    }

    @Nested
    @DisplayName("생성자 및 초기화 테스트")
    class Constructor {

        @Test
        @DisplayName("생성자에서 comcodes Map이 정상 초기화됨")
        void constructor_InitializesComcodesMap() {
            // given
            given(comcodeRepository.findAll()).willReturn(mockComcodeEntities);

            // when
            ComcodeService newService = new ComcodeService(comcodeRepository);

            // then
            Map<String, ComcodeDto> codes = newService.getCodes();
            assertThat(codes).hasSize(8);
            assertThat(codes).containsKeys("USER", "ADMIN", "PENDING", "ACCEPTED", "REJECTED",
                "RATING", "RANKING", "SOLVED_COUNT");
        }
    }
} 