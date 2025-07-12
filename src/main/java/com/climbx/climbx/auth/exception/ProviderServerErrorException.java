package com.climbx.climbx.auth.exception;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.common.error.BusinessException;
import com.climbx.climbx.common.error.ErrorCode;

public class ProviderServerErrorException extends BusinessException {

    public ProviderServerErrorException(OAuth2ProviderType providerType) {
        super(ErrorCode.PROVIDER_TOKEN_EXCHANGE_FAILED);
        addContext("providerType", providerType.name());
    }
} 