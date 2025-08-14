package com.climbx.climbx.problem.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;

public class GymAreaNotFoundException extends BusinessException {

    public GymAreaNotFoundException(Long gymAreaId) {
        super(ErrorCode.GYM_AREA_NOT_FOUND, "GymArea not found with id: " + gymAreaId);
    }
}