package com.climbx.climbx.auth.provider.exception;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class InvalidNonceException extends BusinessException {

    public InvalidNonceException(OAuth2ProviderType providerType) {
        super(ErrorCode.INVALID_TOKEN);
        addContext("providerType", providerType.name());
        addContext("reason", "nonce mismatch");
    }
} 