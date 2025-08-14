package com.climbx.climbx.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CallbackRequestDto(

    @NotBlank
    String idToken, // OpenID 토큰

    String nonce // replay attack 방지를 위한 nonce 값, nullable
) {

}