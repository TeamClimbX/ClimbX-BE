package com.climbx.climbx.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserProfileInfoModifyRequestDto(

    @Size(min = 1, max = 64)
    String newNickname,

    @Size(max = 128)
    String newStatusMessage
) {

}