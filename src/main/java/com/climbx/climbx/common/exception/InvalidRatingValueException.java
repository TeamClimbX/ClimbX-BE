package com.climbx.climbx.common.exception;

import com.climbx.climbx.common.enums.ErrorCode;

public class InvalidRatingValueException extends BusinessException {

    public InvalidRatingValueException(int rating) {
        super(ErrorCode.INVALID_RATING_VALUE);
        addContext("rating", String.valueOf(rating));
    }
}
