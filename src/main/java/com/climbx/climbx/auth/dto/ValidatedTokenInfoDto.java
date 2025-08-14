package com.climbx.climbx.auth.dto;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import lombok.Builder;

/**
 * OAuth2 ID Token 검증 결과를 담는 DTO
 */
@Builder
public record ValidatedTokenInfoDto(

    String providerId,
    String nickname,
    String email,
    String profileImageUrl,
    OAuth2ProviderType providerType
) {

}