package com.climbx.climbx.common.comcode;

import com.climbx.climbx.common.comcode.dto.ComcodeDto;
import com.climbx.climbx.common.comcode.entity.ComcodeEntity;
import com.climbx.climbx.common.comcode.exception.ComcodeNotFound;
import com.climbx.climbx.common.comcode.repository.ComcodeRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComcodeService {

    private final ComcodeRepository comcodeRepository;
    private Map<String, ComcodeDto> comcodes;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.comcodes = getCodes();
    }

    public Map<String, ComcodeDto> getCodes() {
        return comcodeRepository.findAll()
            .stream()
            .collect(Collectors.toMap(
                ComcodeEntity::code,
                ComcodeDto::from
            ));
    }

    public List<ComcodeDto> getCodesByGroup(String groupCode) {
        return comcodes.values()
            .stream()
            .filter(code -> code.codeGroup().equals(groupCode))
            .toList(); // 필터링된 불변 리스트 반환
    }

    /**
     * 특정 코드에 대한 DTO를 반환합니다. 존재 하지 않는 코드에 대해서 exception
     */
    public ComcodeDto getCodeDto(String code) {
        return Optional.ofNullable(comcodes.get(code))
            .orElseThrow(() -> new ComcodeNotFound(code));
    }

    public String getCodeValue(String code) {
        return getCodeDto(code).code();
    }
}
