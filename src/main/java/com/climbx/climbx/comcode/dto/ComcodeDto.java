package com.climbx.climbx.comcode.dto;

import com.climbx.climbx.comcode.entity.ComcodeEntity;
import lombok.Builder;

@Builder
public record ComcodeDto(

    String codeGroup,
    String code,
    String codeName,
    Integer sortOrder
) {

    public static ComcodeDto from(ComcodeEntity entity) {
        return ComcodeDto.builder()
            .codeGroup(entity.groupCode())
            .code(entity.code())
            .codeName(entity.codeName())
            .sortOrder(entity.sortOrder())
            .build();
    }
}
