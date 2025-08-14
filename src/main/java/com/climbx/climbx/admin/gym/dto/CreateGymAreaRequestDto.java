package com.climbx.climbx.admin.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGymAreaRequestDto(

    @NotBlank(message = "구역 이름은 필수입니다.")
    @Size(min = 1, max = 64, message = "구역 이름은 1자 이상 64자 이하여야 합니다.")
    String areaName
) {

}
