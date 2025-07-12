package com.climbx.climbx.auth.provider;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.auth.exception.ProviderNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ProviderFactory {

    private final Map<OAuth2ProviderType, OAuth2Provider> providers;

    public OAuth2ProviderFactory(List<OAuth2Provider> providerList) {
        this.providers = providerList.stream()
            .collect(Collectors.toMap(
                OAuth2Provider::getProviderType,
                Function.identity()
            ));
    }

    /**
     * providerType에 따라 구현체를 반환
     */
    public OAuth2Provider getProvider(OAuth2ProviderType providerType) {
        OAuth2Provider provider = providers.get(providerType);
        if (provider == null) {
            throw new ProviderNotSupportedException(providerType);
        }
        return provider;
    }

    /**
     * 문자열 받아서 Provider 구현체 반환
     */
    public OAuth2Provider getProvider(String providerName) {
        OAuth2ProviderType providerType = OAuth2ProviderType.fromString(providerName);
        return getProvider(providerType);
    }
} 