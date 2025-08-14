package com.climbx.climbx.video.exception;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import java.util.UUID;

public class VideoNotFoundException extends BusinessException {

    public VideoNotFoundException(UUID videoId) {
        super(ErrorCode.VIDEO_NOT_FOUND);
        addContext("videoId", videoId.toString());
    }
}
