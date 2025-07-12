package com.climbx.climbx.auth.provider;

import com.climbx.climbx.auth.dto.OAuth2TokenResponseDto;
import com.climbx.climbx.auth.dto.OAuth2UserInfoDto;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;

public interface OAuth2Provider {

    OAuth2TokenResponseDto exchangeCodeForToken(String code);

    OAuth2UserInfoDto fetchUserInfo(String accessToken);

    OAuth2ProviderType getProviderType();
} 