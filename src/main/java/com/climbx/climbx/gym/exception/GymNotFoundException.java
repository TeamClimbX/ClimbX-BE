package com.climbx.climbx.gym.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class GymNotFoundException extends BusinessException {

    public GymNotFoundException(Long gymId) {
        super(ErrorCode.GYM_NOT_FOUND);
        addContext("gymId", gymId.toString());
    }
} 