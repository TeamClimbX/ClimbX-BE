package com.climbx.climbx.auth.provider.exception;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class ProviderNotSupportedException extends BusinessException {

    public ProviderNotSupportedException(OAuth2ProviderType provider) {
        super(ErrorCode.PROVIDER_NOT_SUPPORTED);
        addContext("provider", provider.name());
    }

    public ProviderNotSupportedException(String provider) {
        super(ErrorCode.PROVIDER_NOT_SUPPORTED);
        addContext("provider", provider);
    }
}
