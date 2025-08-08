package com.climbx.climbx.fixture;

import com.climbx.climbx.auth.entity.UserAuthEntity;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.user.entity.UserAccountEntity;

public class UserAuthFixture {

    public static final String DEFAULT_PROVIDER_ID = "12345";
    public static final String DEFAULT_PROVIDER_EMAIL = "test@example.com";
    public static final OAuth2ProviderType DEFAULT_PROVIDER_TYPE = OAuth2ProviderType.KAKAO;
    public static final boolean DEFAULT_IS_PRIMARY = true;

    // 제공자 타입과 함께 UserAuthEntity 생성
    public static UserAuthEntity createUserAuth(
        UserAccountEntity userAccount,
        OAuth2ProviderType providerType
    ) {
        return createUserAuth(
            userAccount,
            providerType,
            DEFAULT_PROVIDER_ID,
            DEFAULT_PROVIDER_EMAIL,
            DEFAULT_IS_PRIMARY
        );
    }

    // 제공자 타입과 주 인증 여부와 함께 UserAuthEntity 생성
    public static UserAuthEntity createUserAuth(
        UserAccountEntity userAccount,
        OAuth2ProviderType providerType,
        boolean isPrimary
    ) {
        return createUserAuth(
            userAccount,
            providerType,
            DEFAULT_PROVIDER_ID,
            DEFAULT_PROVIDER_EMAIL,
            isPrimary
        );
    }

    // 모든 필드를 지정하여 UserAuthEntity 생성
    public static UserAuthEntity createUserAuth(
        UserAccountEntity userAccount,
        OAuth2ProviderType providerType,
        String providerId,
        String providerEmail,
        boolean isPrimary
    ) {
        return UserAuthEntity.builder()
            .userAccountEntity(userAccount)
            .provider(providerType)
            .providerId(providerId)
            .providerEmail(providerEmail)
            .isPrimary(isPrimary)
            .build();
    }
} 